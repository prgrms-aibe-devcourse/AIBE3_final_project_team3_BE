package triplestar.mixchat.global.notifiaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import triplestar.mixchat.domain.notification.dto.NotificationResp;
import triplestar.mixchat.domain.notification.service.NotificationService;

/**
 * 알림 이벤트 리스너
 * - AFTER_COMMIT에서 비동기로 실행
 * - DB 저장 (독립 트랜잭션) + WebSocket 전송
 * - 빠른 INSERT이므로 비동기 처리 시 DB 부담 최소화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Async("chatEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleNotificationEvent(NotificationEvent event) {
        try {
            log.info("알림 이벤트 처리 시작 - receiverId: {}, type: {}", event.receiverId(), event.type());

            // DB에 알림 저장 (독립 트랜잭션)
            NotificationResp notification = notificationService.createNotification(event);

            // WebSocket으로 실시간 알림 전송
            String destination = "/queue/notifications";
            messagingTemplate.convertAndSendToUser(
                    event.receiverId().toString(),
                    destination,
                    notification
            );

            log.info("알림 전송 완료 - receiverId: {}, type: {}", event.receiverId(), event.type());
        } catch (Exception e) {
            log.error("알림 처리 실패 - receiverId: {}, type: {}",
                    event.receiverId(), event.type(), e);
            // 예외를 던지지 않고 로깅만 (알림 실패가 메시지 저장에 영향 없도록)
        }
    }
}
