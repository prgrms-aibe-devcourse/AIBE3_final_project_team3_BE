package triplestar.mixchat.domain.chat.chat.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.DirectChatRoomResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.DirectChatRoom;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.chat.repository.DirectChatRoomRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.cache.ChatAuthCacheService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DirectChatRoomService {

    private final DirectChatRoomRepository directChatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatAuthCacheService chatAuthCacheService;
    private final ChatMessageService chatMessageService;
    private final ChatMemberService chatMemberService;
    private final SystemMessageService systemMessageService;
    private final triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository chatMessageRepository;
    // todo: 각 서비스 Facade패턴 도입 고려

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new AccessDeniedException("사용자를 찾을 수 없습니다. ID: " + memberId));
    }

    @Transactional
    public DirectChatRoomResp findOrCreateDirectChatRoom(Long member1Id, Long member2Id, String senderNickname) {
        Member member1 = findMemberById(member1Id);
        Member member2 = findMemberById(member2Id);

        Long smallerId = Math.min(member1.getId(), member2.getId());
        Long largerId = Math.max(member1.getId(), member2.getId());

        // 1:1 채팅방 조회
        DirectChatRoom room = directChatRoomRepository.findByUser1_IdAndUser2_Id(smallerId, largerId)
                .orElse(null);

        if (room == null) {
            // 방이 없으면 생성
            DirectChatRoom newRoom = DirectChatRoom.create(member1, member2);
            room = directChatRoomRepository.save(newRoom);

            // ChatMember 생성 및 저장
            chatRoomMemberRepository.save(new ChatMember(member1, room.getId(), ChatRoomType.DIRECT));
            chatRoomMemberRepository.save(new ChatMember(member2, room.getId(), ChatRoomType.DIRECT));

            // 캐시 관리
            chatAuthCacheService.addMember(room.getId(), member1Id);
            chatAuthCacheService.addMember(room.getId(), member2Id);

            // DTO 변환 및 알림
            DirectChatRoomResp roomDto = DirectChatRoomResp.from(room, 0L, null);
            messagingTemplate.convertAndSendToUser(member1.getId().toString(), "/queue/rooms", roomDto);
            messagingTemplate.convertAndSendToUser(member2.getId().toString(), "/queue/rooms", roomDto);

            // 채팅 시작 첫 메시지 전송
            systemMessageService.sendDirectChatStartedMessage(room.getId(), ChatRoomType.DIRECT);
        } else {
            // 방이 있으면 멤버십 복구 확인 (나갔다가 다시 들어오는 경우)
            restoreMemberIfMissing(room.getId(), member1, member1Id);
            restoreMemberIfMissing(room.getId(), member2, member2Id);
        }

        return DirectChatRoomResp.from(room, 0L, null);
    }

    private void restoreMemberIfMissing(Long roomId, Member member, Long memberId) {
        if (!chatRoomMemberRepository.existsByChatRoomIdAndChatRoomTypeAndMember_Id(roomId, ChatRoomType.DIRECT, memberId)) {
            chatRoomMemberRepository.save(new ChatMember(member, roomId, ChatRoomType.DIRECT));
            chatAuthCacheService.addMember(roomId, memberId);
            // 재입장 시 시스템 메시지 전송
            systemMessageService.sendJoinMessage(roomId, member.getNickname(), ChatRoomType.DIRECT);
        }
    }

    // 1:1 채팅방 나가기
    @Transactional
    public void leaveRoom(Long roomId, Long currentUserId) {
        // 나가는 사람의 닉네임 조회 (나가기 전에 미리 조회)
        Member member = findMemberById(currentUserId);
        String nickname = member.getNickname();

        // 방 나가기 처리 (마지막 사람이면 방 삭제됨)
        chatMemberService.leaveRoom(currentUserId, roomId, ChatRoomType.DIRECT);

        // 방이 아직 존재한다면(상대방이 남아있다면) 시스템 메시지 전송
        if (directChatRoomRepository.existsById(roomId)) {
            systemMessageService.sendLeaveMessage(roomId, nickname, ChatRoomType.DIRECT);
        }
    }

    // 사용자가 참여하고 있는 1:1 채팅방 목록 조회
    public List<DirectChatRoomResp> getRoomsForUser(Long currentUserId) {
        // 한 번의 쿼리로 채팅방 정보(유저 포함)와 lastReadSequence 조회
        List<Object[]> results = directChatRoomRepository.findRoomsAndLastReadByMemberId(currentUserId);

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        // DTO로 변환하여 반환
        return results.stream()
                .map(result -> {
                    DirectChatRoom room = (DirectChatRoom) result[0];
                    Long lastRead = (Long) result[1];

                    long unreadCount = (lastRead == null) ? room.getCurrentSequence() : room.getCurrentSequence() - lastRead;
                    if (unreadCount < 0) unreadCount = 0; // 예외 상황에 대비

                    // 마지막 메시지 조회 (번역된 메시지가 있으면 번역된 내용 사용)
                    String lastMessageContent = chatMessageRepository
                            .findTopByChatRoomIdAndChatRoomTypeOrderBySequenceDesc(room.getId(), ChatRoomType.DIRECT)
                            .map(msg -> msg.isTranslateEnabled() && msg.getTranslatedContent() != null
                                    ? msg.getTranslatedContent()
                                    : msg.getContent())
                            .orElse(null);

                    return DirectChatRoomResp.from(room, unreadCount, lastMessageContent);
                })
                .collect(Collectors.toList());
    }
}
