package triplestar.mixchat.domain.member.friend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import triplestar.mixchat.domain.member.member.entity.Member;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "friendship_requests")
@Getter
public class FriendshipRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    @CreatedDate
    private LocalDateTime createdAt;

    public FriendshipRequest(Member sender, Member receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }
}
