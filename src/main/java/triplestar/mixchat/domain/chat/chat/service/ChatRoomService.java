package triplestar.mixchat.domain.chat.chat.service;


import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.dto.ChatRoomResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.ChatRoom;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.cache.ChatAuthCacheService;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatAuthCacheService chatAuthCacheService; // 캐시 서비스 주입

    // 구독 가능한 채팅방 패턴
    private static final Pattern ROOM_DESTINATION_PATTERN = Pattern.compile("^/topic/rooms/(\\d+)$\u0022);


    //== 인가(Authorization) 관련 메서드 ==//

    // STOMP destination 경로에서 채팅방 ID(Long)를 추출
    public Long getRoomIdFromDestination(String destination) {
        if (destination == null) return null;
        Matcher matcher = ROOM_DESTINATION_PATTERN.matcher(destination);
        if (matcher.matches()) {
            return Long.parseLong(matcher.group(1));
        }
        return null;
    }

    // 사용자가 해당 채팅방의 멤버인지 확인 (캐시 적용)
    @Transactional(readOnly = true)
    public void verifyUserIsMemberOfRoom(Long memberId, Long roomId) {
        if (roomId == null || memberId == null) {
            throw new AccessDeniedException("사용자 또는 채팅방 정보가 유효하지 않습니다.");
        }

        // 1. 캐시에서 먼저 확인
        if (chatAuthCacheService.isMember(roomId, memberId)) {
            return; // 캐시에 존재하면 DB 조회 없이 바로 통과
        }

        // 2. 캐시에 없으면 DB 조회 (Cache Miss)
        boolean isMember = chatRoomMemberRepository.existsByChatRoom_IdAndMember_Id(memberId, roomId);
        if (!isMember) {
            log.warn("인가 거부: 사용자(ID:{})가 채팅방(ID:{})의 멤버가 아닙니다.", memberId, roomId);
            throw new AccessDeniedException("해당 채팅방에 접근할 권한이 없습니다.");
        }

        // 3. DB에 존재하면, 그 결과를 캐시에 저장 (다음 조회를 위해)
        log.debug("DB check passed for user {} in room {}. Caching the result.", memberId, roomId);
        chatAuthCacheService.addMember(roomId, memberId);
    }


    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + memberId));
    }

    @Transactional
    public ChatRoomResp findOrCreateDirectRoom(Long member1Id, Long member2Id) {
        Member member1 = findMemberById(member1Id);
        Member member2 = findMemberById(member2Id);

        ChatRoom room = chatRoomRepository.findDirectRoomByMembers(member1, member2)
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.createDirectRoom(member1.getNickname(), member2.getNickname());

                    ChatMember chatMember1 = new ChatMember(member1, newRoom, ChatMember.UserType.ROOM_MEMBER);
                    ChatMember chatMember2 = new ChatMember(member2, newRoom, ChatMember.UserType.ROOM_MEMBER);

                    newRoom.getMembers().add(chatMember1);
                    newRoom.getMembers().add(chatMember2);

                    ChatRoom savedRoom = chatRoomRepository.save(newRoom);

                    ChatRoomResp roomDto = ChatRoomResp.from(savedRoom); // roomDto 생성

                    // 웹소켓 메시지 발송 (DTO 사용)
                    messagingTemplate.convertAndSendToUser(member1.getId().toString(), "/topic/rooms", roomDto);
                    messagingTemplate.convertAndSendToUser(member2.getId().toString(), "/topic/rooms", roomDto);

                    return savedRoom; // <-- orElseGet 람다는 ChatRoom 엔티티를 반환해야 함
                });
        // 메서드의 최종 반환은 ChatRoom 엔티티를 DTO로 변환하여 반환
        return ChatRoomResp.from(room);
    }

    @Transactional
    public ChatRoomResp createGroupRoom(String roomName, List<Long> memberIds, Long creatorId) {
        Member creator = findMemberById(creatorId);
        ChatRoom newRoom = ChatRoom.createGroupRoom(roomName);

        List<Member> members = memberRepository.findAllById(memberIds);
        if (!members.contains(creator)) {
            members.add(creator);
        }

        List<ChatMember> chatMembers = members.stream().map(member -> {
            ChatMember.UserType userType = member.equals(creator) ? ChatMember.UserType.ROOM_OWNER : ChatMember.UserType.ROOM_MEMBER;
            return new ChatMember(member, newRoom, userType);
        }).collect(Collectors.toList());

        newRoom.getMembers().addAll(chatMembers);

        ChatRoom savedRoom = chatRoomRepository.save(newRoom);

        ChatRoomResp roomDto = ChatRoomResp.from(savedRoom);
        members.forEach(member -> {
            messagingTemplate.convertAndSendToUser(member.getId().toString(), "/topic/rooms", roomDto);
        });

        return roomDto;
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResp> getRoomsForUser(Long currentUserId) {
        Member currentUser = findMemberById(currentUserId);
        return chatRoomRepository.findAllByMember(currentUser).stream()
                .map(ChatRoomResp::from)
                .collect(Collectors.toList());
    }



    public ChatRoom getRoom(Long id) {
        return chatRoomRepository.findById(id).orElseThrow(() -> new RuntimeException("채팅방 없음"));
    }

    @Transactional
    public ChatRoomResp createPublicGroupRoom(String roomName, Long creatorId) {
        List<Long> allMemberIds = memberRepository.findAll().stream()
                .map(Member::getId)
                .collect(Collectors.toList());

        return createGroupRoom(roomName, allMemberIds, creatorId);
    }

    @Transactional
    public void leaveRoom(Long roomId, Long currentUserId) {
        // todo : 방장 퇴장 이후 처리 필요
        Member currentUser = findMemberById(currentUserId);
        ChatRoom room = getRoom(roomId);
        ChatMember memberToRemove = room.getMembers().stream()
                .filter(cm -> cm.getMember().equals(currentUser))
                .findFirst()
                .orElseThrow(() -> new SecurityException("채팅방에 속해있지 않습니다."));

        room.getMembers().remove(memberToRemove);

        if (room.getRoomType() == ChatRoom.RoomType.GROUP && room.getMembers().isEmpty()) {
            chatRoomRepository.delete(room);
        } else {
            chatRoomRepository.save(room);
        }
    }

    @Transactional
    public void blockUser(Long roomId, Long currentUserId) {
        Member currentUser = findMemberById(currentUserId);
        ChatRoom room = getRoom(roomId);
        log.info("User {} with id {} initiated a block in room {}", currentUser.getEmail(), currentUserId, roomId);
    }

    @Transactional
    public void reportUser(Long roomId, Long currentUserId) {
        Member currentUser = findMemberById(currentUserId);
        ChatRoom room = getRoom(roomId);
        log.info("User {} with id {} reported room {}", currentUser.getEmail(), currentUserId, roomId);
    }
}