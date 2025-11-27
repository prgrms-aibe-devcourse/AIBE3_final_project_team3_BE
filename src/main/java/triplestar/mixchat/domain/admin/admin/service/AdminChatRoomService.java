package triplestar.mixchat.domain.admin.admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.entity.GroupChatRoom;
import triplestar.mixchat.domain.chat.chat.repository.AIChatRoomRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.chat.repository.DirectChatRoomRepository;
import triplestar.mixchat.domain.chat.chat.repository.GroupChatRoomRepository;
import triplestar.mixchat.domain.notification.constant.NotificationType;
import triplestar.mixchat.global.cache.ChatAuthCacheService;
import triplestar.mixchat.global.notifiaction.NotificationEvent;

@Service
@RequiredArgsConstructor
public class AdminChatRoomService {
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final DirectChatRoomRepository directChatRoomRepository;
    private final GroupChatRoomRepository groupChatRoomRepository;
    private final AIChatRoomRepository aiChatRoomRepository;

    private final ChatAuthCacheService chatAuthCacheService;

    @Transactional
    public void forceCloseRoom(Long roomId, ChatMessage.chatRoomType roomType) {
        // 1) 모든 멤버 조회 (알림 보내기 위해 필요)
        List<ChatMember> members = chatRoomMemberRepository
                .findByChatRoomIdAndChatRoomType(roomId, roomType);

        // 1. 채팅방 조회
        GroupChatRoom room = groupChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹 채팅방입니다."));

        // 1️⃣ 해당 방의 모든 멤버 삭제 (DB)
       chatRoomMemberRepository.deleteAll(members);

        // 2️⃣ Redis 캐시도 삭제
        chatAuthCacheService.removeRoom(roomId);

        // 3️⃣ 방 자체 삭제
        switch (roomType) {
            case DIRECT -> directChatRoomRepository.deleteById(roomId);
            case GROUP -> groupChatRoomRepository.deleteById(roomId);
            case AI -> aiChatRoomRepository.deleteById(roomId);
            default -> throw new IllegalArgumentException("지원하지 않는 타입입니다.");
        }

        // 5) 🔥 멤버들에게 방 폐쇄 알림(NotificationEvent 발행)
        for (ChatMember m : members) {
            NotificationEvent event = new NotificationEvent(
                    m.getMember().getId(),
                    adminId, // 관리자 ID
                    NotificationType.CHAT_ROOM_CLOSED,
                    "채팅방이 관리자에 의해 폐쇄되었습니다. 방 ID: " + roomId
            );

            eventPublisher.publishEvent(event);
        }
    }
}
