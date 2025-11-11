package triplestar.mixchat.testutils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.security.CustomUserDetails;

@Profile("test")
@Service
public class TestUserDetailsService implements UserDetailsService {

    @Autowired
    private MemberRepository memberRepository;

    // 테스트에서는 nickname 으로 유저를 찾도록
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByNickname(username)
                .orElseThrow(() -> new UsernameNotFoundException(username + " 테스트 회원이 존재하지 않습니다."));

        return new CustomUserDetails(member.getId(), member.getRole());
    }
}
