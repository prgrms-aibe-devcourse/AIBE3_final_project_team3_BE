package triplestar.mixchat.domain.chat.chat.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.ai.userprompt.entity.UserPrompt;
import triplestar.mixchat.domain.ai.userprompt.repository.UserPromptRepository;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.AIChatRoomResp;
import triplestar.mixchat.domain.chat.chat.dto.CreateAIChatReq;
import triplestar.mixchat.domain.chat.chat.entity.AIChatRoom;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.repository.AIChatRoomRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.cache.ChatAuthCacheService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AIChatRoomService {

    private final AIChatRoomRepository aiChatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatAuthCacheService chatAuthCacheService;
    private final ChatMemberService chatMemberService;
    private final UserPromptRepository userPromptRepository;

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new AccessDeniedException("사용자를 찾을 수 없습니다. ID: " + memberId));
    }

    @Transactional
    public AIChatRoomResp createAIChatRoom(Long creatorId, CreateAIChatReq req) {
        Member creator = findMemberById(creatorId);

        UserPrompt persona = userPromptRepository.findById(req.personaId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 페르소나 ID입니다. ID: " + req.personaId()));
        AIChatRoom newRoom = AIChatRoom.create(req.roomName(), persona, req.roomType());
        AIChatRoom savedRoom = aiChatRoomRepository.save(newRoom);

        ChatMember chatMember = new ChatMember(creator, savedRoom.getId(), ChatRoomType.AI);
        chatRoomMemberRepository.save(chatMember);

        chatAuthCacheService.addMember(savedRoom.getId(), creatorId);

        return AIChatRoomResp.from(savedRoom);
    }

    public void verifyUserIsMemberOfRoom(Long memberId, Long roomId) {
        chatMemberService.verifyUserIsMemberOfRoom(memberId, roomId, ChatRoomType.AI);
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
        chatMemberService.leaveRoom(currentUserId, roomId, ChatRoomType.AI);
    }
}

