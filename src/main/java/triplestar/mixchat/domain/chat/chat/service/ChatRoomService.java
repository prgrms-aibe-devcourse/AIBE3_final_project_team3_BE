package triplestar.mixchat.domain.chat.chat.service;


import lombok.RequiredArgsConstructor;
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
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatRoom findOrCreateDirectRoom(Member member1, Long member2Id) {
        Member member2 = memberRepository.findById(member2Id)
                .orElseThrow(() -> new RuntimeException("채팅 상대를 찾을 수 없습니다. ID: " + member2Id));

        return chatRoomRepository.findDirectRoomByMembers(member1, member2)
                .orElseGet(() -> {
                    ChatRoom newRoom = new ChatRoom();
                    newRoom.setRoomType(ChatRoom.RoomType.DIRECT);
                    newRoom.setName(member1.getNickname() + ", " + member2.getNickname());

                    ChatMember chatMember1 = new ChatMember();
                    chatMember1.setMember(member1);
                    chatMember1.setChatRoom(newRoom);
                    chatMember1.setUserType(ChatMember.UserType.ROOM_MEMBER);

                    ChatMember chatMember2 = new ChatMember();
                    chatMember2.setMember(member2);
                    chatMember2.setChatRoom(newRoom);
                    chatMember2.setUserType(ChatMember.UserType.ROOM_MEMBER);

                    newRoom.getMembers().add(chatMember1);
                    newRoom.getMembers().add(chatMember2);

                    ChatRoom savedRoom = chatRoomRepository.save(newRoom);

                    ChatRoomResp roomDto = ChatRoomResp.from(savedRoom);
                    messagingTemplate.convertAndSend("/topic/user/" + member1.getId() + "/rooms", roomDto);
                    messagingTemplate.convertAndSend("/topic/user/" + member2.getId() + "/rooms", roomDto);

                    return savedRoom;
                });
    }

    @Transactional
    public ChatRoom createGroupRoom(String roomName, List<Long> memberIds, Member creator) {
        ChatRoom newRoom = new ChatRoom();
        newRoom.setRoomType(ChatRoom.RoomType.GROUP);
        newRoom.setName(roomName);

        List<Member> members = memberRepository.findAllById(memberIds);
        members.add(creator);

        List<ChatMember> chatMembers = members.stream().map(member -> {
            ChatMember chatMember = new ChatMember();
            chatMember.setMember(member);
            chatMember.setChatRoom(newRoom);
            if (member.equals(creator)) {
                chatMember.setUserType(ChatMember.UserType.ROOM_OWNER);
            } else {
                chatMember.setUserType(ChatMember.UserType.ROOM_MEMBER);
            }
            return chatMember;
        }).collect(Collectors.toList());

        newRoom.getMembers().addAll(chatMembers);

        ChatRoom savedRoom = chatRoomRepository.save(newRoom);

        ChatRoomResp roomDto = ChatRoomResp.from(savedRoom);
        members.forEach(member -> {
            messagingTemplate.convertAndSend("/topic/user/" + member.getId() + "/rooms", roomDto);
        });

        return savedRoom;
    }

    public List<ChatRoom> getRoomsForUser(Member currentUser) {
        return chatRoomRepository.findAllByMember(currentUser);
    }

    public ChatRoom getRoom(Long id) {
        return chatRoomRepository.findById(id).orElseThrow(() -> new RuntimeException("채팅방 없음"));
    }

    @Transactional
    public ChatRoom createPublicGroupRoom(String roomName, Member creator) {
        List<Member> allMembers = memberRepository.findAll();
        List<Long> allMemberIds = allMembers.stream()
                .map(Member::getId)
                .collect(Collectors.toList());

        return createGroupRoom(roomName, allMemberIds, creator);
    }

    @Transactional
    public void leaveRoom(Long roomId, Member currentUser) {
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
    public void blockUser(Long roomId, Member currentUser) {
        ChatRoom room = getRoom(roomId);
        System.out.println("User " + currentUser.getEmail() + " initiated a block in room " + roomId);
    }

    @Transactional
    public void reportUser(Long roomId, Member currentUser) {
        ChatRoom room = getRoom(roomId);
        System.out.println("User " + currentUser.getEmail() + " reported room " + roomId);
    }
}