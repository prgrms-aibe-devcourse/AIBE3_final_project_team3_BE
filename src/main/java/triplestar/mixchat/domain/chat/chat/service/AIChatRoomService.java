package triplestar.mixchat.domain.chat.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.dto.AIChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.CreateAIChatReq;
import triplestar.mixchat.domain.chat.chat.entity.AIChatRoom;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.repository.AIChatRoomRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.cache.ChatAuthCacheService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AIChatRoomService {

    private final AIChatRoomRepository aiChatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatAuthCacheService chatAuthCacheService;
    private final ChatInteractionService chatInteractionService;

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new AccessDeniedException("사용자를 찾을 수 없습니다. ID: " + memberId));
    }

    @Transactional
    public AIChatRoomResp createAIChatRoom(Long creatorId, CreateAIChatReq request) {
        Member creator = findMemberById(creatorId);
        AIChatRoom newRoom = AIChatRoom.create("AI Chat", "gpt-4", "Friendly Assistant"); // Example values
        AIChatRoom savedRoom = aiChatRoomRepository.save(newRoom);

        ChatMember chatMember = new ChatMember(creator, savedRoom.getId(), ChatMessage.chatRoomType.AI);
        chatRoomMemberRepository.save(chatMember);

        chatAuthCacheService.addMember(savedRoom.getId(), creatorId);

        return AIChatRoomResp.from(savedRoom);
    }

    public void verifyUserIsMemberOfRoom(Long memberId, Long roomId) {
        chatInteractionService.verifyUserIsMemberOfRoom(memberId, roomId, ChatMessage.chatRoomType.AI);
    }

    public List<AIChatRoomResp> getRoomsForUser(Long currentUserId) {
        Member currentUser = findMemberById(currentUserId);
        List<AIChatRoom> rooms = aiChatRoomRepository.findAllByMember(currentUser);
        return rooms.stream()
                .map(AIChatRoomResp::from)
                .collect(Collectors.toList());
    }

    public AIChatRoom getAIChatRoom(Long id) {
        return aiChatRoomRepository.findById(id).orElseThrow(() -> new RuntimeException("AI 채팅방 없음"));
    }

    // AI 채팅방 나가기
    @Transactional
    public void leaveAIChatRoom(Long roomId, Long currentUserId) {
        chatInteractionService.leaveRoom(currentUserId, roomId, ChatMessage.chatRoomType.AI);
    }
}

