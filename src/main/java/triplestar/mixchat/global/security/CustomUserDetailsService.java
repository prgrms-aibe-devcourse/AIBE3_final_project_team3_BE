package triplestar.mixchat.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return memberRepository.findById(Long.parseLong(username))
                .map(member -> new CustomUserDetails(member.getId(), member.getRole()))
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자"));
    }
}