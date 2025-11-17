package triplestar.mixchat.domain.notification.event;

import org.springframework.lang.Nullable;
import triplestar.mixchat.domain.notification.constant.NotificationType;

public record NotificationEvent(
    Long receiverId,
    @Nullable Long senderId,
    NotificationType type,
    @Nullable String extraContent
) {
    public NotificationEvent {
        validate(receiverId, type);
    }

    private void validate(Long receiverId, NotificationType type) {
        if (receiverId == null) {
            throw new IllegalArgumentException("receiverId는 null일 수 없습니다.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type은 null일 수 없습니다.");
        }
    }

    public NotificationEvent(Long receiverId, Long senderId, NotificationType type) {
        this(receiverId, senderId, type, null);
    }
}
