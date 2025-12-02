package triplestar.mixchat.domain.chat.chat.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
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
    // todo: 각 서비스 Facade패턴 도입 고려

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new AccessDeniedException("사용자를 찾을 수 없습니다. ID: " + memberId));
    }

    // 사용자가 해당 1:1 채팅방의 멤버인지 확인
    public void verifyUserIsMemberOfRoom(Long memberId, Long roomId) {
        chatMemberService.verifyUserIsMemberOfRoom(memberId, roomId, ChatRoomType.DIRECT);
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
            DirectChatRoomResp roomDto = DirectChatRoomResp.from(room, 0L);
            messagingTemplate.convertAndSendToUser(member1.getId().toString(), "/topic/rooms", roomDto);
            messagingTemplate.convertAndSendToUser(member2.getId().toString(), "/topic/rooms", roomDto);

            // 채팅 시작 첫 메시지 전송
            systemMessageService.sendDirectChatStartedMessage(room.getId(), ChatRoomType.DIRECT);
        } else {
            // 방이 있으면 멤버십 복구 확인 (나갔다가 다시 들어오는 경우)
            restoreMemberIfMissing(room.getId(), member1, member1Id);
            restoreMemberIfMissing(room.getId(), member2, member2Id);
        }
        
        return DirectChatRoomResp.from(room, 0L);
    }

    private void restoreMemberIfMissing(Long roomId, Member member, Long memberId) {
        if (!chatRoomMemberRepository.existsByChatRoomIdAndChatRoomTypeAndMember_Id(roomId, ChatRoomType.DIRECT, memberId)) {
            chatRoomMemberRepository.save(new ChatMember(member, roomId, ChatRoomType.DIRECT));
            chatAuthCacheService.addMember(roomId, memberId);
        }
    }

    // 1:1 채팅방 나가기
    @Transactional
    public void leaveRoom(Long roomId, Long currentUserId) {
        chatMemberService.leaveRoom(currentUserId, roomId, ChatRoomType.DIRECT);
    }

    // 사용자가 참여하고 있는 1:1 채팅방 목록 조회
    public List<DirectChatRoomResp> getRoomsForUser(Long currentUserId) {
        Member currentUser = findMemberById(currentUserId);
        // ChatMember 엔티티를 통해 사용자가 속한 1:1 채팅방 ID와 마지막 읽은 위치를 조회
        List<ChatMember> chatMembers = chatRoomMemberRepository.findByMemberAndChatRoomType(currentUser, ChatRoomType.DIRECT);

        if (chatMembers.isEmpty()) {
            return Collections.emptyList();
        }

        // roomId를 키로, lastReadSequence를 값으로 하는 맵 생성
        Map<Long, Long> lastReadSequenceMap = chatMembers.stream()
                .collect(Collectors.toMap(ChatMember::getChatRoomId, ChatMember::getLastReadSequence, (seq1, seq2) -> seq1));

        List<Long> directRoomIds = chatMembers.stream()
                .map(ChatMember::getChatRoomId)
                .collect(Collectors.toList());

        // 조회된 ID들로 DirectChatRoom 엔티티들을 조회
        List<DirectChatRoom> directRooms = directChatRoomRepository.findAllById(directRoomIds);

        // DTO로 변환하여 반환
        return directRooms.stream()
                .map(room -> {
                    Long lastRead = lastReadSequenceMap.get(room.getId());
                    long unreadCount = (lastRead == null) ? room.getCurrentSequence() : room.getCurrentSequence() - lastRead;
                    if (unreadCount < 0) unreadCount = 0; // 방어적 코드
                    return DirectChatRoomResp.from(room, unreadCount);
                })
                .collect(Collectors.toList());
    }
}
