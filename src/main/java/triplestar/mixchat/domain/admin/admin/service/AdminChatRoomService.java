package triplestar.mixchat.domain.admin.admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.admin.admin.constant.RoomCloseReason;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.GroupChatRoom;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.chat.repository.GroupChatRoomRepository;
import triplestar.mixchat.domain.notification.constant.NotificationType;
import triplestar.mixchat.global.cache.ChatAuthCacheService;
import triplestar.mixchat.global.notifiaction.NotificationEvent;
import triplestar.mixchat.global.websocket.RoomClosedEventPublisher;

@Service
@RequiredArgsConstructor
public class AdminChatRoomService {
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final GroupChatRoomRepository groupChatRoomRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ChatAuthCacheService chatAuthCacheService;
    private final RoomClosedEventPublisher roomClosedEventPublisher;

    @Transactional
    public void forceCloseRoom(Long roomId, Long adminId, int reasonCode) {
        RoomCloseReason reason = RoomCloseReason.fromCode(reasonCode);

        // 1. 채팅방 조회
        GroupChatRoom room = groupChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹 채팅방입니다."));

        String roomName = room.getName();

        // 2. 모든 멤버 조회 (알림 보내기 위해 필요)
        List<ChatMember> members = chatRoomMemberRepository
                .findByChatRoomIdAndChatRoomType(roomId, ChatRoomType.GROUP);

        // 3. 해당 방의 모든 멤버 삭제 (DB)
       chatRoomMemberRepository.deleteByChatRoomIdAndChatRoomType(roomId, ChatRoomType.GROUP);

        // 4. Redis 캐시도 삭제
        chatAuthCacheService.removeRoom(roomId);

        // 5. 방 자체 삭제
        groupChatRoomRepository.deleteById(roomId);

        // 6. 멤버들에게 방 폐쇄 알림(NotificationEvent 발행)
        String message = String.format("[%s] 채팅방이 '%s' 사유로 관리자에 의해 폐쇄되었습니다.", roomName, reason.getLabel());
        for (ChatMember m : members) {
            NotificationEvent event = new NotificationEvent(
                    m.getMember().getId(),
                    adminId,
                    NotificationType.ROOM_CLOSED,
                    message
            );
            eventPublisher.publishEvent(event);
        }

        roomClosedEventPublisher.sendRoomClosed(roomId, roomName, reason.getLabel());
    }
}
