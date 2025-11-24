package triplestar.mixchat.domain.chat.chat.service;


import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections; // 추가
import java.util.Map; // 추가
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.dto.CreateGroupChatReq;
import triplestar.mixchat.domain.chat.chat.dto.GroupChatRoomResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.entity.GroupChatRoom;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.chat.repository.GroupChatRoomRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.cache.ChatAuthCacheService;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupChatRoomService {

    private final GroupChatRoomRepository groupChatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatAuthCacheService chatAuthCacheService;
    private final ChatInteractionService chatInteractionService;

    @Transactional(readOnly = true)
    public void verifyUserIsMemberOfRoom(Long memberId, Long roomId) {
        chatInteractionService.verifyUserIsMemberOfRoom(memberId, roomId, ChatMessage.chatRoomType.GROUP);
    }


    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new AccessDeniedException("사용자를 찾을 수 없습니다. ID: " + memberId));
    }

    // 방 조회 공통 기능
    @Transactional
    public GroupChatRoomResp createGroupRoom(CreateGroupChatReq request, Long creatorId) {
        Member creator = findMemberById(creatorId);
        GroupChatRoom newRoom = GroupChatRoom.create(request.roomName(), request.description(), request.topic(), request.password());

        List<Member> members = memberRepository.findAllById(request.memberIds());
        if (!members.contains(creator)) {
            members.add(creator);
        }

        GroupChatRoom savedRoom = groupChatRoomRepository.save(newRoom);

        List<ChatMember> chatMembers = members.stream().map(member -> {
            ChatMember.UserType userType = member.equals(creator) ? ChatMember.UserType.ROOM_OWNER : ChatMember.UserType.ROOM_MEMBER;
            return new ChatMember(member, savedRoom.getId(), ChatMessage.chatRoomType.GROUP, userType);
        }).collect(Collectors.toList());
        chatRoomMemberRepository.saveAll(chatMembers);

        chatMembers.forEach(cm ->
                chatAuthCacheService.addMember(savedRoom.getId(), cm.getMember().getId())
        );

        GroupChatRoomResp roomDto = GroupChatRoomResp.from(savedRoom, chatMembers);
        members.forEach(member -> {
            messagingTemplate.convertAndSendToUser(member.getId().toString(), "/topic/rooms", roomDto);
        });

        return roomDto;
    }

    // 사용자가 속해있는 그룹채팅방 조회(chat 페이지 용도)
    @Transactional(readOnly = true)
    public List<GroupChatRoomResp> getRoomsForUser(Long currentUserId) {
        List<GroupChatRoom> rooms = groupChatRoomRepository.findAllByMemberId(currentUserId);
        return convertToRoomResponses(rooms);
    }

    // 기존에 만들어진 그룹채팅방 조회(find 페이지의 Groups 탭 용도)
    @Transactional(readOnly = true)
    public List<GroupChatRoomResp> getGroupPublicRooms(Long currentUserId) {
        List<GroupChatRoom> rooms = groupChatRoomRepository.findPublicRoomsExcludingMemberId(currentUserId);
        return convertToRoomResponses(rooms);
    }

    @Transactional
    public void leaveRoom(Long roomId, Long currentUserId) {
        chatInteractionService.leaveRoom(currentUserId, roomId, ChatMessage.chatRoomType.GROUP);
    }

    @Transactional
    public void blockUser(Long roomId, Long currentUserId, Long blockedUserId) {
        chatInteractionService.blockUser(currentUserId, blockedUserId, roomId, ChatMessage.chatRoomType.GROUP);
    }

    @Transactional
    public void reportUser(Long roomId, Long currentUserId, Long reportedUserId, String reason) {
        chatInteractionService.reportUser(currentUserId, reportedUserId, roomId, ChatMessage.chatRoomType.GROUP, reason);
    }

    //그룹 채팅방 목록을 DTO로 변환
    private List<GroupChatRoomResp> convertToRoomResponses(List<GroupChatRoom> rooms) {
        if (rooms.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 모든 방의 ID 수집
        List<Long> roomIds = rooms.stream()
                .map(GroupChatRoom::getId)
                .collect(Collectors.toList());

        // 2. 모든 방의 멤버를 한 번의 쿼리로 조회
        List<ChatMember> allMembers = chatRoomMemberRepository.findAllByRoomIdsWithMember(roomIds);

        // 3. roomId를 키로 하는 Map으로 그룹화
        Map<Long, List<ChatMember>> membersByRoom = allMembers.stream()
                .collect(Collectors.groupingBy(ChatMember::getChatRoomId));

        // 4. DTO 변환
        return rooms.stream()
                .map(room -> GroupChatRoomResp.from(
                        room,
                        membersByRoom.getOrDefault(room.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }
}