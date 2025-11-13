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
        return Member.createMember(
                username + "@example.com",
                Password.encrypt("user1234", passwordEncoder),
                username,
                username,
                Country.CANADA,
                EnglishLevel.INTERMEDIATE,
                List.of("음악"),
                "테스트 회원입니다."
        );
    }
}
