package triplestar.mixchat.domain.member.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.constant.MembershipGrade;
import triplestar.mixchat.domain.member.member.constant.Role;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor
public class Member extends BaseEntity {

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Embedded
    private Password password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Country country;

    @Column(nullable = false)
    private String interest;

    @Column(nullable = false)
    private EnglishLevel englishLevel;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MembershipGrade membershipGrade;

    private LocalDateTime lastSignInAt;

    @Column(nullable = false)
    private boolean isBlocked;

    private LocalDateTime blockedAt;

    @Column(nullable = false)
    private boolean isDeleted;

    private LocalDateTime deletedAt;

    private String blockReason;

    @Builder
    public Member(String email, Password password, String name, String nickname, Country country,
                  EnglishLevel englishLevel, String interest, String description) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.country = country;
        this.englishLevel = englishLevel;
        this.interest = interest;
        this.description = description;
    }
}
