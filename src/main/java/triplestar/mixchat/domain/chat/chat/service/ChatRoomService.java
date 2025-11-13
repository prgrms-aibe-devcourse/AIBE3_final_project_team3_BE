package triplestar.mixchat.domain.chat.chat.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.dto.ChatRoomResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.ChatRoom;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;

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

                    ChatRoomResp roomDto = ChatRoomResp.from(savedRoom);

                    messagingTemplate.convertAndSend("/topic/user/" + member1.getId() + "/rooms", roomDto);
                    messagingTemplate.convertAndSend("/topic/user/" + member2.getId() + "/rooms", roomDto);

                    return savedRoom;
                });
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
            messagingTemplate.convertAndSend("/topic/user/" + member.getId() + "/rooms", roomDto);
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
