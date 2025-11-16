package triplestar.mixchat.domain.notification.event;

import triplestar.mixchat.domain.notification.constant.NotificationType;

public record NotificationEvent(
    Long receiverId,
    String nickname,
    NotificationType type,
    String extraContent
) {
    public static NotificationEvent createWithoutContent(Long receiverId, String nickname, NotificationType type) {
        return new NotificationEvent(receiverId, nickname, type, null);
    }
}
