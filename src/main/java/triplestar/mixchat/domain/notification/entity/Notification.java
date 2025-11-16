package triplestar.mixchat.domain.notification.entity;


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

    private NotificationType type;

    private String content;

    private boolean isRead;

    public Notification(Member receiver, NotificationType type, String content) {
        this.receiver = receiver;
        this.type = type;
        this.content = content;
    }

    public void read() {
        this.isRead = true;
    }
}
