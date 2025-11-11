package triplestar.mixchat.domain.member.friend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.global.jpa.entity.BaseEntityNoModified;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "friendships")
public class Friendship extends BaseEntityNoModified {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "smaller_member_id", nullable = false)
    private Member smallerMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "larger_member_id", nullable = false)
    private Member largerMember;

    public Friendship(Member smallerMember, Member largerMember) {
        this.smallerMember = smallerMember;
        this.largerMember = largerMember;
    }
}
