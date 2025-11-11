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

    @Transactional
    public ChatRoom findOrCreateDirectRoom(Member member1, Long member2Id) {
        Member member2 = memberRepository.findById(member2Id)
                .orElseThrow(() -> new RuntimeException("채팅 상대를 찾을 수 없습니다. ID: " + member2Id));

        return chatRoomRepository.findDirectRoomByMembers(member1, member2)
                .orElseGet(() -> {
                    ChatRoom newRoom = new ChatRoom();
                    newRoom.setRoomType(ChatRoom.RoomType.DIRECT);
                    newRoom.setName(member1.getNickname() + ", " + member2.getNickname());

                    ChatMember chatMember1 = new ChatMember(member1, newRoom, ChatMember.UserType.ROOM_MEMBER);
                    ChatMember chatMember2 = new ChatMember(member2, newRoom, ChatMember.UserType.ROOM_MEMBER);

                    newRoom.getMembers().add(chatMember1);
                    newRoom.getMembers().add(chatMember2);

                    ChatRoom savedRoom = chatRoomRepository.save(newRoom);

                    ChatRoomResp roomDto = ChatRoomResp.from(savedRoom);

                    // 1:1 채팅방의 두 사용자에게 웹소켓을 통해 채팅방 생성/업데이트 이벤트를 실시간으로 전송합니다.
                    // 클라이언트는 이 메시지를 받아 채팅방 목록을 자동으로 갱신할 수 있습니다.
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
            ChatMember.UserType userType = member.equals(creator) ? ChatMember.UserType.ROOM_OWNER : ChatMember.UserType.ROOM_MEMBER;
            return new ChatMember(member, newRoom, userType);
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
        // todo : 방장 퇴장 이후 처리 필요
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
        log.info("User {} initiated a block in room {}", currentUser.getEmail(), roomId);
    }

    @Transactional
    public void reportUser(Long roomId, Member currentUser) {
        ChatRoom room = getRoom(roomId);
        log.info("User {} reported room {}", currentUser.getEmail(), roomId);
    }
}
