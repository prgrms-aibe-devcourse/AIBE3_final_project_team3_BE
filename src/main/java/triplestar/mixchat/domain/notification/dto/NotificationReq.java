package triplestar.mixchat.domain.notification.dto;

import triplestar.mixchat.domain.notification.constant.NotificationType;

public record NotificationReq(
    Long receiverId,
    NotificationType type,
    String content
) {
}
