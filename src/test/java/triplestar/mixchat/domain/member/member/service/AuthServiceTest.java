package triplestar.mixchat.domain.member.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.auth.service.AuthService;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.auth.dto.MemberJoinReq;
import triplestar.mixchat.domain.member.auth.dto.SigninReq;
import triplestar.mixchat.domain.member.auth.dto.MemberSummaryResp;
import triplestar.mixchat.domain.member.auth.dto.SignInResp;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.customException.UniqueConstraintException;
import triplestar.mixchat.global.security.jwt.AuthJwtProvider;

@ActiveProfiles("test")
@DisplayName("회원 - 인증 서비스")
@Transactional
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthJwtProvider authJwtProvider;

    private MemberSummaryResp joinDummy(String email) {
        MemberJoinReq memberJoinReq = new MemberJoinReq(
                email,
                "user1234",
                "user1234",
                "홍길동",
                "UK",
                "loveCoding",
                EnglishLevel.NATIVE,
                List.of("프로그래밍 좋아함"),
                "다른 것도 좋아함"
        );
        return authService.join(memberJoinReq);
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void join_success() {
        MemberSummaryResp memberDto = joinDummy("test@test.com");

        Long id = memberDto.id();
        Member member = memberRepository.findById(id).orElseThrow(AssertionError::new);

        assertThat(member.getEmail()).isEqualTo("test@test.com");
        assertThat(member.getName()).isEqualTo("홍길동");
        assertThat(member.getCountry().getCode()).isEqualTo("UK");
        assertThat(member.getNickname()).isEqualTo("loveCoding");
        assertThat(member.getEnglishLevel()).isEqualTo(EnglishLevel.NATIVE);
        assertThat(member.getInterests()).isEqualTo(List.of("프로그래밍 좋아함"));
        assertThat(member.getDescription()).isEqualTo("다른 것도 좋아함");
    }

    @Test
    @DisplayName("회원가입 - 성공 이메일 제외하고는 중복 허용")
    void join_success_non_unique() {
        joinDummy("test@test1.com");
        joinDummy("test@test2.com");
    }

    @Test
    @DisplayName("회원가입 - 실패 이메일은 고유값")
    void join_fail_duplicateEmail() {
        Assertions.assertThatThrownBy(() -> {
            joinDummy("test@test1.com");
            joinDummy("test@test1.com");
        }).isInstanceOf(UniqueConstraintException.class);
    }

    @Test
    @DisplayName("회원가입 - 실패 패스워드 확인 불일치")
    void join_fail_password_confirm() {
        MemberJoinReq memberJoinReq = new MemberJoinReq(
                "test@test.com",
                "user1234",
                "user12345",
                "홍길동",
                "UK",
                "loveCoding",
                EnglishLevel.NATIVE,
                List.of("프로그래밍 좋아함"),
                "다른 것도 좋아함"
        );

        Assertions.assertThatThrownBy(() -> {
            authService.join(memberJoinReq);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("로그인 - 성공")
    void signIn_success() {
        joinDummy("test@test1.com");

        SignInResp signInResp = authService.signIn(new SigninReq("test@test1.com", "user1234"));
        authJwtProvider.parseAccessToken(signInResp.accessToken());
        authJwtProvider.parseRefreshToken(signInResp.refreshToken());
    }

    @Test
    @DisplayName("로그인 - 실패 등록되지 않은 이메일")
    void signIn_email_not_found() {
        joinDummy("test@test1.com");

        Assertions.assertThatThrownBy(() -> {
            authService.signIn(new SigninReq("undefined@test1.com", "user1234"));
        }).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("로그인 - 실패 비밀번호")
    void signIn_password_fail() {
        joinDummy("test@test1.com");

        Assertions.assertThatThrownBy(() -> {
            authService.signIn(new SigninReq("test@test1.com", "user12345"));
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("토큰 재발급 - 성공 로그인 결과로 나온 refresh token 으로 액세스 토큰 재발급")
    void reissue_success() {
        joinDummy("test@test1.com");
        SignInResp signInResp = authService.signIn(new SigninReq("test@test1.com", "user1234"));
        String refreshToken = signInResp.refreshToken();

        authService.reissueAccessToken(refreshToken);
    }
}