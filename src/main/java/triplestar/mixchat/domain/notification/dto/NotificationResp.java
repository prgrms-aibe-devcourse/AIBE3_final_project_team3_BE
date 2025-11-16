package triplestar.mixchat.domain.notification.dto;

import java.time.LocalDateTime;
import triplestar.mixchat.domain.notification.constant.NotificationType;

public record NotificationResp(
        Long id,
        Long receiverId,
        String nickname,
        NotificationType type,
        LocalDateTime createdAt,
        String content
) {
}
