package triplestar.mixchat.domain.chat.chat.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.dto.AIChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.DirectChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.GroupChatRoomResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.repository.AIChatRoomRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.chat.repository.DirectChatRoomRepository;
import triplestar.mixchat.domain.chat.chat.repository.GroupChatRoomRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.cache.ChatAuthCacheService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatInteractionService {

    private final MemberRepository memberRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatAuthCacheService chatAuthCacheService;
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
            log.warn("인가 거부: 사용자(ID:{})가 대화방(ID:{}, 타입:{})의 멤버가 아닙니다.", memberId, roomId, chatRoomType);
            throw new AccessDeniedException("해당 대화방에 접근할 권한이 없습니다.");
        }

        // 3. DB에 존재하면, 그 결과를 캐시에 저장 (다음 조회를 위해)
        log.debug("DB check passed for user {} in room {} (type {}). Caching the result.", memberId, roomId, chatRoomType);
        chatAuthCacheService.addMember(roomId, memberId);
    }

    //사용자 차단 (미구현)
    @Transactional
    public void blockUser(Long blockerId, Long blockedId, Long chatRoomId, ChatMessage.chatRoomType chatRoomType) {
        // TODO: 사용자 차단 로직 구현
        log.info("User {} blocked user {} in chatRoom {} (type {}).", blockerId, blockedId, chatRoomId, chatRoomType);
        throw new UnsupportedOperationException("사용자 차단 기능은 아직 구현되지 않았습니다.");
    }

    //사용자/대화방 신고 (미구현)
    @Transactional
    public void reportUser(Long reporterId, Long reportedId, Long chatRoomId, ChatMessage.chatRoomType chatRoomType, String reason) {
        // TODO: 사용자/대화방 신고 로직 구현
        log.info("User {} reported user {} or chatRoom {} (type {}) for reason: {}.", reporterId, reportedId, chatRoomId, chatRoomType, reason);
        throw new UnsupportedOperationException("사용자/대화방 신고 기능은 아직 구현되지 않았습니다.");
    }

    //특정 대화방의 알림 설정 변경
    @Transactional
    public void updateNotificationSetting(Long memberId, Long roomId, ChatMessage.chatRoomType chatRoomType, boolean enableNotifications) {
        // TODO: ChatMember의 알림 설정 변경 로직 구현
        log.warn("updateNotificationSetting 메서드는 아직 구현되지 않았습니다.");
        throw new UnsupportedOperationException("알림 설정 변경 기능은 아직 구현되지 않았습니다.");
    }

    //특정 대화방에서 사용자의 마지막 읽은 메시지 업데이트 (미구현)
    @Transactional
    public void updateLastReadMessage(Long memberId, Long roomId, ChatMessage.chatRoomType chatRoomType, String lastReadMessageId) {
        // TODO: ChatMember의 lastReadAt 또는 lastReadMessageId 업데이트 로직 구현
        log.warn("updateLastReadMessage 메서드는 아직 구현되지 않았습니다.");
        throw new UnsupportedOperationException("마지막 읽은 메시지 업데이트 기능은 아직 구현되지 않았습니다.");
    }

    /**
     * 대화방 나가기 로직
     * @param memberId 나가는 사용자 ID
     * @param roomId 대화방 ID
     * @param chatRoomType 대화방 타입
     */
    @Transactional
    public void leaveRoom(Long memberId, Long roomId, ChatMessage.chatRoomType chatRoomType) {
        // 1. ChatMember 찾기 및 삭제
        ChatMember memberToRemove = chatRoomMemberRepository
                .findByChatRoomIdAndChatRoomTypeAndMember_Id(roomId, chatRoomType, memberId)
                .orElseThrow(() -> new SecurityException("해당 대화방에 속해있지 않습니다."));
        chatRoomMemberRepository.delete(memberToRemove);

        // 2. 캐시에서 멤버 제거
        chatAuthCacheService.removeMember(roomId, memberId);

        // 3. 남은 멤버 수 확인 후 대화방 삭제 (해당 타입의 방에만 적용)
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
}

