package triplestar.mixchat.domain.chat.chat.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
public class DirectChatRoomService {

    private final DirectChatRoomRepository directChatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatAuthCacheService chatAuthCacheService;
    private final ChatMessageService chatMessageService;
    private final ChatInteractionService chatInteractionService;

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new AccessDeniedException("사용자를 찾을 수 없습니다. ID: " + memberId));
    }

    //사용자가 해당 1:1 채팅방의 멤버인지 확인 (ChatInteractionService로 위임)
    @Transactional(readOnly = true)
    public void verifyUserIsMemberOfRoom(Long memberId, Long roomId) {
        chatInteractionService.verifyUserIsMemberOfRoom(memberId, roomId, ChatMessage.ConversationType.DIRECT);
    }

    @Transactional
    public DirectChatRoomResp findOrCreateDirectChatRoom(Long member1Id, Long member2Id, String senderNickname) {
        Member member1 = findMemberById(member1Id);
        Member member2 = findMemberById(member2Id);

        Long smallerId = Math.min(member1.getId(), member2.getId());
        Long largerId = Math.max(member1.getId(), member2.getId());

        // 1:1 채팅방 조회 후 없으면 생성
        DirectChatRoom room = directChatRoomRepository.findByUser1_IdAndUser2_Id(smallerId, largerId) // 정렬된 ID를 쿼리에 사용
                .orElseGet(() -> {
                    //Member 객체를 정적팩토리메서드 DirectChatRoom.create에 전달
                    DirectChatRoom newRoom = DirectChatRoom.create(member1, member2);
                    DirectChatRoom savedRoom = directChatRoomRepository.save(newRoom);

                    // ChatMember 생성 및 저장
                    chatRoomMemberRepository.save(new ChatMember(member1, savedRoom.getId(), ChatMessage.ConversationType.DIRECT, ChatMember.UserType.ROOM_MEMBER));
                    chatRoomMemberRepository.save(new ChatMember(member2, savedRoom.getId(), ChatMessage.ConversationType.DIRECT, ChatMember.UserType.ROOM_MEMBER));

                    // 캐시 관리
                    chatAuthCacheService.addMember(savedRoom.getId(), member1Id);
                    chatAuthCacheService.addMember(savedRoom.getId(), member2Id);

                    // DTO 변환
                    DirectChatRoomResp roomDto = DirectChatRoomResp.from(savedRoom);

                    // 웹소켓 메시지 발송
                    messagingTemplate.convertAndSendToUser(member1.getId().toString(), "/topic/rooms", roomDto);
                    messagingTemplate.convertAndSendToUser(member2.getId().toString(), "/topic/rooms", roomDto);

                    // 채팅 시작 첫 메시지 전송 (TODO 해결)
                    chatMessageService.saveMessage(savedRoom.getId(), member1Id, senderNickname, "1:1 채팅이 시작되었습니다.", ChatMessage.MessageType.SYSTEM, ChatMessage.ConversationType.DIRECT);

                    return savedRoom;
                });
        
        // DTO 변환
        return DirectChatRoomResp.from(room);
    }

    //1:1 채팅방 나가기 (ChatInteractionService로 위임)
    @Transactional
    public void leaveRoom(Long roomId, Long currentUserId) {
        chatInteractionService.leaveRoom(currentUserId, roomId, ChatMessage.ConversationType.DIRECT);
    }

    //1:1 채팅방 사용자 차단 (ChatInteractionService로 위임)
    @Transactional
    public void blockUser(Long roomId, Long currentUserId, Long blockedUserId) {
        // TODO: ChatInteractionService에 blockUser 로직 구현 후 위임
        log.warn("blockUser (DirectChatRoom) 메서드는 아직 구현되지 않았습니다.");
        throw new UnsupportedOperationException("1:1 채팅방 사용자 차단 기능은 아직 구현되지 않았습니다.");
    }

    //1:1 채팅방/사용자 신고 (ChatInteractionService로 위임)
    @Transactional
    public void reportUser(Long roomId, Long currentUserId, Long reportedUserId, String reason) {
        chatInteractionService.reportUser(currentUserId, reportedUserId, roomId, ChatMessage.ConversationType.DIRECT, reason);
    }

    // 사용자가 참여하고 있는 1:1 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<DirectChatRoomResp> getRoomsForUser(Long currentUserId) {
        Member currentUser = findMemberById(currentUserId);
        // ChatMember 엔티티를 통해 사용자가 속한 1:1 채팅방 ID들을 조회
        List<ChatMember> chatMembers = chatRoomMemberRepository.findByMemberAndConversationType(currentUser, ChatMessage.ConversationType.DIRECT);

        List<Long> directRoomIds = chatMembers.stream()
                .map(ChatMember::getChatRoomId)
                .collect(Collectors.toList());

        // 조회된 ID들로 DirectChatRoom 엔티티들을 조회
        List<DirectChatRoom> directRooms = directChatRoomRepository.findAllById(directRoomIds);

        // DTO로 변환하여 반환
        return directRooms.stream()
                .map(DirectChatRoomResp::from)
                .collect(Collectors.toList());
    }
}
