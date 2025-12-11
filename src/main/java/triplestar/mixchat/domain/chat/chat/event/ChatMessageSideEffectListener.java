package triplestar.mixchat.domain.chat.chat.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.dto.RoomLastMessageUpdateResp;
import triplestar.mixchat.domain.chat.chat.service.ChatNotificationService;

/**
 * 방향 A 패턴: AFTER_COMMIT에서는 외부 I/O만 수행
 * - WebSocket broadcast (룸 리스트 업데이트, 메시지 전송)
 * - DB 연산은 모두 saveMessage 트랜잭션 내에서 처리됨
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageSideEffectListener {

    private final ChatNotificationService chatNotificationService;

    @Async("chatEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAfterCommit(ChatMessageCreatedEvent event) {
        try {
            // AI 채팅방은 saveAiRoomMessage에서 별도 처리
            if (event.chatRoomType() == ChatRoomType.AI) {
                return;
            }

            // === 외부 I/O만 수행 (WebSocket broadcast) ===

            // 1. 룸 리스트 업데이트 브로드캐스트
            RoomLastMessageUpdateResp updateResp = new RoomLastMessageUpdateResp(
                    event.roomId(),
                    event.chatRoomType(),
                    event.createdAt().toString(),
                    event.sequence(),
                    event.content()
            );
            chatNotificationService.sendRoomListUpdateBroadcast(updateResp);

            // 2. 메시지 브로드캐스트 (unreadCount는 이미 saveMessage에서 계산됨)
            MessageResp response = new MessageResp(
                    event.messageId(),
                    event.senderId(),
                    event.senderNickname(),
                    event.content(),
                    null, // translatedContent는 이후 번역 완료 시 별도 이벤트로 전송
                    event.translateEnabled(),
                    event.createdAt(),
                    event.messageType(),
                    event.sequence(),
                    event.unreadCount()
            );
            chatNotificationService.sendChatMessage(event.roomId(), event.chatRoomType(), response);

        } catch (Exception e) {
            log.error("WebSocket broadcast 중 예외 발생 - messageId: {}, roomId: {}",
                    event.messageId(), event.roomId(), e);
            // 예외를 던지지 않고 로깅만 (외부 I/O 실패가 메인 흐름에 영향 없도록)
        }
    }
}
