package triplestar.mixchat.global.security;

import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import triplestar.mixchat.domain.member.member.constant.Role;

@Slf4j
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final Role role;
    private final String nickname;

    // 기존 코드를 위한 생성자
    public CustomUserDetails(Long id, Role role) {
        this(id, role, null); // 새로운 생성자를 호출하여 nickname은 null로 설정
    }

    // 채팅 기능을 위해 닉네임을 포함하는 새로운 생성자
    public CustomUserDetails(Long id, Role role, String nickname) {
        this.id = id;
        this.role = role;
        this.nickname = nickname;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(role::name);
    }

    @Override
    @Deprecated
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return id.toString();
    }
}
