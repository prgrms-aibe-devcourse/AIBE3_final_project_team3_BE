package triplestar.mixchat.domain.member.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

@Entity
@Getter
@Table(name = "social_members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SocialMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private SocialProvider socialProvider;

    private String socialId;
}
