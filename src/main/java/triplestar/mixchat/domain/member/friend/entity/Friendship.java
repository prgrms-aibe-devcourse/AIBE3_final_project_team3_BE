package triplestar.mixchat.domain.member.friend.entity;

import jakarta.persistence.Column;
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
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import triplestar.mixchat.domain.member.member.entity.Member;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "friendships")
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "smaller_member_id", nullable = false)
    private Member smallerMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "larger_member_id", nullable = false)
    private Member largerMember;

    @CreatedDate
    private LocalDateTime createdAt;

    public Friendship(Member smallerMember, Member largerMember) {
        this.smallerMember = smallerMember;
        this.largerMember = largerMember;
    }
}
