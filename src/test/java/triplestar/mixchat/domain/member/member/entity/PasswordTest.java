package triplestar.mixchat.domain.member.member.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("해시 비밀번호")
class PasswordTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("비밀번호 생성 테스트")
    void t1() {
        String pw = "test1234";
        Password password = Password.encrypt(pw, passwordEncoder);
        // 해시값 확인용
        // assertThat(password.getPassword()).isEqualTo("");
        assertThat(password.matches("test1234", passwordEncoder)).isTrue();
    }

    @Test
    @DisplayName("비밀번호 실패(길이 미달)")
    void password_too_short() {
        String pw = "test123";
        Assertions.assertThatThrownBy(() -> Password.encrypt(pw, passwordEncoder))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("비밀번호 실패(길이 초과)")
    void password_too_long() {
        String pw = "toolongpassword1234toolongpassword1234toolongpassword1234";
        Assertions.assertThatThrownBy(() -> Password.encrypt(pw, passwordEncoder))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("비밀번호 실패(숫자만)")
    void password_only_digit() {
        String pw = "123456789";
        Assertions.assertThatThrownBy(() -> Password.encrypt(pw, passwordEncoder))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("비밀번호 실패(영어만)")
    void password_only_string() {
        String pw = "abcdefghij";
        Assertions.assertThatThrownBy(() -> Password.encrypt(pw, passwordEncoder))
                .isInstanceOf(IllegalArgumentException.class);
    }
}