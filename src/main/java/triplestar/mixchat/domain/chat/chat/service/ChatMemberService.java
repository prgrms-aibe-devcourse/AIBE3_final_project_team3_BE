package triplestar.mixchat.domain.chat.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.repository.AIChatRoomRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.chat.repository.DirectChatRoomRepository;
import triplestar.mixchat.domain.chat.chat.repository.GroupChatRoomRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.cache.ChatAuthCacheService;
import triplestar.mixchat.global.cache.ChatSubscriberCacheService;
import triplestar.mixchat.domain.chat.chat.dto.SubscriberCountUpdateResp;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatMemberService {

    private final MemberRepository memberRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatAuthCacheService chatAuthCacheService;
    private final ChatSubscriberCacheService chatSubscriberCacheService;
    private final DirectChatRoomRepository directChatRoomRepository;
    private final GroupChatRoomRepository groupChatRoomRepository;
    private final AIChatRoomRepository aiChatRoomRepository;


    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new AccessDeniedException("사용자를 찾을 수 없습니다. ID: " + memberId));
    }

    //사용자가 특정 대화방의 멤버인지 확인 (캐시 적용)
    public void verifyUserIsMemberOfRoom(Long memberId, Long roomId, ChatMessage.chatRoomType chatRoomType) {
        if (roomId == null || memberId == null || chatRoomType == null) {
            throw new AccessDeniedException("사용자, 대화방 정보 또는 대화 타입이 유효하지 않습니다.");
        }

        // 1. 캐시에서 먼저 확인
        if (chatAuthCacheService.isMember(roomId, memberId)) {
            return; // 캐시에 존재하면 DB 조회 없이 바로 통과
        }

        // 2. 캐시에 없으면 DB 조회 (Cache Miss)
        boolean isMember = chatRoomMemberRepository.existsByChatRoomIdAndChatRoomTypeAndMember_Id(
                roomId, chatRoomType, memberId);
        if (!isMember) {
            log.warn("채팅방 접근 거부 - memberId: {}, roomId: {}, type: {}", memberId, roomId, chatRoomType);
            throw new AccessDeniedException("해당 대화방에 접근할 권한이 없습니다.");
        }

        // 3. DB에 존재하면, 그 결과를 캐시에 저장 (다음 조회를 위해)
        chatAuthCacheService.addMember(roomId, memberId);
    }

    // 읽음 처리
    @Transactional
    public void updateLastReadSequence(Long memberId, Long roomId, ChatMessage.chatRoomType chatRoomType, Long sequence) {
        ChatMember member = chatRoomMemberRepository.findByChatRoomIdAndChatRoomTypeAndMember_Id(
                roomId, chatRoomType, memberId
        ).orElseThrow(() -> new AccessDeniedException("해당 대화방에 접근할 권한이 없습니다."));

        member.updateLastReadSequence(sequence);
    }

    // 채팅방 입장 시 자동 읽음 처리 (해당 방의 최신 sequence까지 읽음 처리)
    // 반환값: 실제로 새로 읽은 메시지가 있으면 currentSequence, 없으면 null
    @Transactional
    public Long markAsReadOnEnter(Long memberId, Long roomId, ChatMessage.chatRoomType chatRoomType) {
        ChatMember member = chatRoomMemberRepository.findByChatRoomIdAndChatRoomTypeAndMember_Id(
                roomId, chatRoomType, memberId
        ).orElseThrow(() -> new AccessDeniedException("해당 대화방에 접근할 권한이 없습니다."));

        Long currentSequence = getCurrentSequence(roomId, chatRoomType);
        if (currentSequence != null && currentSequence > 0) {
            Long lastReadSequence = member.getLastReadSequence();

            // 이미 모든 메시지를 읽은 상태면 null 반환 (READ 이벤트 브로드캐스트 하지 않음)
            if (lastReadSequence != null && lastReadSequence >= currentSequence) {
                return null;
            }

            member.updateLastReadSequence(currentSequence);
            return currentSequence;
        }
        return null;
    }

    // 현재 채팅방의 최신 sequence 조회 (DIRECT, GROUP만 사용)
    private Long getCurrentSequence(Long roomId, ChatMessage.chatRoomType chatRoomType) {
        return switch (chatRoomType) {
            case DIRECT -> directChatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId))
                    .getCurrentSequence();
            case GROUP -> groupChatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId))
                    .getCurrentSequence();
            default -> throw new UnsupportedOperationException("지원하지 않는 채팅방 타입입니다: " + chatRoomType);
        };
    }

    // 대화방 나가기
    @Transactional
    public void leaveRoom(Long memberId, Long roomId, ChatMessage.chatRoomType chatRoomType) {
        // 1. ChatMember 찾기 및 삭제
        ChatMember memberToRemove = chatRoomMemberRepository
                .findByChatRoomIdAndChatRoomTypeAndMember_Id(roomId, chatRoomType, memberId)
                .orElseThrow(() -> new AccessDeniedException("해당 대화방에 접근할 권한이 없습니다."));
        chatRoomMemberRepository.delete(memberToRemove);

        // 2. 인증 캐시에서 멤버 제거
        chatAuthCacheService.removeMember(roomId, memberId);

        // 3. Redis 구독자 캐시에서 해당 멤버의 모든 세션 제거
        // WebSocket 연결이 끊기기 전에 HTTP DELETE로 방을 나가는 경우를 대비
        // (유령 구독자 방지 및 정확한 구독자 수 유지)
        Set<String> memberSessions = subscriberCacheService.getSessionsByMemberId(roomId, memberId);
        if (memberSessions != null && !memberSessions.isEmpty()) {
            for (String sessionId : memberSessions) {
                subscriberCacheService.removeSubscriber(roomId, memberId, sessionId);
            }
            log.info("채팅방 퇴장 시 구독자 캐시 정리 완료 - memberId: {}, roomId: {}, 제거된 세션 수: {}",
                    memberId, roomId, memberSessions.size());
        }

        // 4. 남은 멤버 수 확인 후 대화방 삭제 (해당 타입의 방에만 적용)
        long remainingMembersCount = chatRoomMemberRepository.countByChatRoomIdAndChatRoomType(roomId, chatRoomType);

        if (remainingMembersCount == 0) {
            switch (chatRoomType) {
                case DIRECT:
                    directChatRoomRepository.deleteById(roomId);
                    break;
                case GROUP:
                    groupChatRoomRepository.deleteById(roomId);
                    break;
                case AI:
                    aiChatRoomRepository.deleteById(roomId);
                    break;
                default:
                    log.warn("알 수 없는 대화 타입으로 인해 방 삭제에 실패했습니다: {}", chatRoomType);
                    break;
            }
        }
    }

    // TODO: 채팅방에서 특정 사용자 차단 (해당 채팅방에서만 메시지 안 보이게)
    @Transactional
    public void blockUser(Long currentUserId, Long targetUserId, Long roomId, ChatMessage.chatRoomType chatRoomType) {
        throw new UnsupportedOperationException("차단 기능은 아직 구현되지 않았습니다.");
    }

    // TODO: 채팅방에서 특정 사용자 신고
    @Transactional
    public void reportUser(Long currentUserId, Long targetUserId, Long roomId, ChatMessage.chatRoomType chatRoomType, String reason) {
        throw new UnsupportedOperationException("신고 기능은 아직 구현되지 않았습니다.");
    }

    // 채팅방의 현재 구독자 수 조회 (Redis)
    public int getSubscriberCount(Long roomId) {
        Set<String> subscribers = chatSubscriberCacheService.getSubscribers(roomId);
        return (subscribers != null) ? subscribers.size() : 0;
    }

    // 채팅방의 전체 멤버 수 조회 (DB)
    public int getTotalMemberCount(Long roomId, ChatMessage.chatRoomType chatRoomType) {
        return (int) chatRoomMemberRepository.countByChatRoomIdAndChatRoomType(roomId, chatRoomType);
    }
}

