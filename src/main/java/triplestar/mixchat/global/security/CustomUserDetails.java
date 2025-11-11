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

    public CustomUserDetails(Long id, Role role) {
        this.id = id;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(role::name);
    }

    @Override
    @Deprecated
    public String getPassword() {
        log.warn("getPassword () 호출됨 : JWT 기반 인증에서 임의로 호출되어서는 안 됩니다.");
        return "";
    }

    @Override
    public String getUsername() {
        return id.toString();
    }
}
