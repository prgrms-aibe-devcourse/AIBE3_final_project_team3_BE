package triplestar.mixchat.domain.notification.dto;

import java.time.LocalDateTime;
import triplestar.mixchat.domain.notification.constant.NotificationType;

public record NotificationResp(
        Long id,
        Long receiverId,
        NotificationType type,
        String content,
        LocalDateTime createdAt
) {
}
