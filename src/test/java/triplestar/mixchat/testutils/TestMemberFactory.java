package triplestar.mixchat.testutils;

import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.entity.Password;

public class TestMemberFactory {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static Member createMember(String username) {
        Member member = Member.createMember(
                username + "@example.com",
                Password.encrypt("user1234", passwordEncoder),
                username,
                username,
                Country.CA,
                EnglishLevel.INTERMEDIATE,
                List.of("음악"),
                "테스트 회원입니다."
        );
        member.updateProfileImageUrl("profile/uuid-1234.png");
        return member;
    }

    public static Member createAdmin(String username) {
        Member admin = Member.createAdmin(
                username + "@example.com",
                Password.encrypt("user1234", passwordEncoder),
                username,
                username,
                Country.CA,
                EnglishLevel.INTERMEDIATE,
                List.of("음악"),
                "테스트 관리자입니다."
        );
        admin.updateProfileImageUrl("profile/uuid-1234.png");
        return admin;
    }
}