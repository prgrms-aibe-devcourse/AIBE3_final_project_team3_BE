package triplestar.mixchat.domain.member.member.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.constant.MembershipGrade;
import triplestar.mixchat.domain.member.member.constant.Role;
import triplestar.mixchat.domain.report.report.constant.ReportCategory;
import triplestar.mixchat.global.converter.JsonListConverter;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Table(name = "members")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @Convert(converter = JsonListConverter.class)
    private List<String> interests;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
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

    private String profileImageUrl;

    private Member(String email, Password password, String name, String nickname, Country country,
                  EnglishLevel englishLevel, List<String> interests, String description, Role role) {
        validate(email, password, name, nickname, country, englishLevel, interests, description, role);
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.country = country;
        this.englishLevel = englishLevel;
        this.interests = interests;
        this.description = description;
        this.role = role;
        this.membershipGrade = MembershipGrade.BASIC;
        this.isBlocked = false;
        this.isDeleted = false;
    }

    private void validate(String email, Password password, String name, String nickname, Country country,
                          EnglishLevel englishLevel, List<String> interests, String description, Role role) {
        validateSignInInfo(email, password);
        validateProfileInfo(name, nickname, country, englishLevel, interests, description);
        if (role == null) {
            throw new IllegalArgumentException("역할 정보는 null일 수 없습니다.");
        }
    }

    private void validateSignInInfo(String email, Password password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 공백일 수 없습니다.");
        }
        if (password == null) {
            throw new IllegalArgumentException("비밀번호는 null일 수 없습니다.");
        }
    }

    private void validateProfileInfo(String name, String nickname, Country country, EnglishLevel englishLevel,
                                  List<String> interests, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 공백일 수 없습니다.");
        }
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 공백일 수 없습니다.");
        }
        if (country == null) {
            throw new IllegalArgumentException("국가 정보는 null일 수 없습니다.");
        }
        if (englishLevel == null) {
            throw new IllegalArgumentException("영어 레벨은 null일 수 없습니다.");
        }
        if (interests == null || interests.isEmpty()) {
            throw new IllegalArgumentException("관심사 목록은 비어있을 수 없습니다.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("자기소개는 공백일 수 없습니다.");
        }
    }

    public static Member createMember(String email, Password password, String name, String nickname, Country country,
                                      EnglishLevel englishLevel, List<String> interests, String description) {
        return new Member(email, password, name, nickname, country,
                englishLevel, interests, description, Role.ROLE_MEMBER);
    }

    public static Member createAdmin(String email, Password password, String name, String nickname, Country country,
                                      EnglishLevel englishLevel, List<String> interests, String description) {
        return new Member(email, password, name, nickname, country,
                englishLevel, interests, description, Role.ROLE_ADMIN);
    }

    public void updateInfo(String name, String nickname, Country country,
                           EnglishLevel englishLevel, List<String> interests, String description) {
        validateProfileInfo(name, nickname, country, englishLevel, interests, description);
        this.name = name;
        this.nickname = nickname;
        this.country = country;
        this.englishLevel = englishLevel;
        this.interests = interests;
        this.description = description;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void blockByReport(ReportCategory category) {
        this.isBlocked = true;
        this.blockedAt = LocalDateTime.now();
        this.blockReason = category.name();
    }

    public boolean isPremium() {
        return this.membershipGrade == MembershipGrade.PREMIUM;
    }

    public void changeMembershipGrade(MembershipGrade grade) {
        this.membershipGrade = grade;
    }
}
