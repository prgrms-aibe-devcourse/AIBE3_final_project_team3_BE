package triplestar.mixchat.domain.member.member.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.regex.Pattern;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Password {

    private String password;

    // 8자 이상, 최소 1개의 영문(소문자/대문자)과 1개의 숫자를 포함을 확인하는 정규표현식
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d).{8,}$");

    private Password(String password) {
        this.password = password;
    }

    public static Password encrypt(String raw, PasswordEncoder encoder) {
        validate(raw);
        return new Password(encoder.encode(raw));
    }

    public boolean matches(String raw, PasswordEncoder encoder) {
        return encoder.matches(raw, this.password);
    }

    private static void validate(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 공백일 수 없습니다.");
        }

        if (!PASSWORD_PATTERN.matcher(raw).matches()) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이며, 최소 1개의 영문과 1개의 숫자를 포함해야 합니다.");
        }
    }
}