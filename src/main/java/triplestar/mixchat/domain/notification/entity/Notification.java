package triplestar.mixchat.domain.notification.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.notification.constant.NotificationType;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

@Entity
@Getter
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Member receiver;

    @Column(nullable = false)
    private NotificationType type;

    private String content;

    @Column(nullable = false)
    private boolean isRead;

    private Notification(Member receiver, NotificationType type, String content) {
        validate(receiver, type, content);
        this.receiver = receiver;
        this.type = type;
        this.content = content;
    }

    private void validate(Member receiver, NotificationType type, String content) {
        if (receiver == null) {
            throw new IllegalArgumentException("수신자는 null일 수 없습니다.");
        }
        if (type == null) {
            throw new IllegalArgumentException("알림 유형은 null일 수 없습니다.");
        }
    }

    public static Notification create(Member receiver, NotificationType type, String content) {
        return new Notification(receiver, type, content);
    }

    public static Notification createWithoutContent(Member receiver, NotificationType type) {
        return new Notification(receiver, type, null);
    }

    public void read() {
        this.isRead = true;
    }
}
