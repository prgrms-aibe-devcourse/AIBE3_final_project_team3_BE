package triplestar.mixchat.global.notifiaction.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import triplestar.mixchat.domain.notification.dto.NotificationResp;
import triplestar.mixchat.domain.notification.service.NotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener
    public void handleNotificationEvent(NotificationEvent event) {
        // DB에 알림 저장
        NotificationResp notification = notificationService.createNotification(event);

        String destination = "/queue/notifications/";

        try {
            // 개인 사용자에게 웹소켓 알림 전송
            messagingTemplate.convertAndSendToUser(
                    event.receiverId().toString(),
                    destination,
                    notification
            );
            log.info("알림 전송 완료 - receiverId: {}, type: {}", event.receiverId(), event.type());
        } catch (Exception e) {
            log.error("알림 전송 실패 - receiverId: {}, type: {}, error: {}",
                    event.receiverId(), event.type(), e.getMessage());
        }
    }
}
