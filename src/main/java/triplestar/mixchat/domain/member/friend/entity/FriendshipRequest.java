package triplestar.mixchat.domain.member.friend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.global.jpa.entity.BaseEntityNoModified;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "friendship_requests")
@Getter
public class FriendshipRequest extends BaseEntityNoModified {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    public FriendshipRequest(Member sender, Member receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }
}
