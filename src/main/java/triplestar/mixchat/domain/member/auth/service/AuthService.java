package triplestar.mixchat.domain.member.auth.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.auth.dto.MemberJoinReq;
import triplestar.mixchat.domain.member.auth.dto.SigninReq;
import triplestar.mixchat.domain.member.auth.dto.MemberSummaryResp;
import triplestar.mixchat.domain.member.auth.dto.SignInResp;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.entity.Password;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.customException.UniqueConstraintException;
import triplestar.mixchat.global.security.jwt.AccessTokenPayload;
import triplestar.mixchat.global.security.jwt.AuthJwtProvider;
import triplestar.mixchat.global.security.redis.redisTokenRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthJwtProvider authJwtProvider;
    private final redisTokenRepository redisTokenRepository;

    /**
     * 회원가입
     */
    public MemberSummaryResp join(MemberJoinReq req) {
        validateJoinReq(req);
        Member member = buildJoinMember(req);

        Member savedMember = memberRepository.save(member);
        return new MemberSummaryResp(savedMember);
    }

    private void validateJoinReq(MemberJoinReq req) {
        String reqEmail = req.email();
        if (memberRepository.existsByEmail(reqEmail)) {
            throw new UniqueConstraintException("이미 사용중인 이메일입니다: " + reqEmail);
        }

        String reqPassword = req.password();
        String reqPasswordConfirm = req.passwordConfirm();
        if (!reqPassword.equals(reqPasswordConfirm)) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
    }

    private Member buildJoinMember(MemberJoinReq req) {
        return Member.builder()
                .email(req.email())
                .password(Password.encrypt(req.password(), passwordEncoder))
                .name(req.name())
                .nickname(req.nickname())
                .country(Country.findByCode(req.country()))
                .englishLevel(req.englishLevel())
                .interests(req.interest())
                .description(req.description())
                .build();
    }

    /**
     * 로그인
     */
    public SignInResp signIn(SigninReq req) {
        Member member = memberRepository.findByEmail(req.email())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이메일입니다: " + req.email()));

        if (!member.getPassword().matches(req.password(), passwordEncoder)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = authJwtProvider.generateAccessToken(
                new AccessTokenPayload(member.getId(), member.getRole()));

        String refreshToken = authJwtProvider.generateRefreshToken(member.getId());

        redisTokenRepository.save(member.getId(), refreshToken);

        return new SignInResp(accessToken, refreshToken);
    }

    /**
     * 액세스 토큰 재발급
     */
    public SignInResp reissueAccessToken(String reqRefreshToken) {
        Long memberId = authJwtProvider.parseRefreshToken(reqRefreshToken);

        // Redis에 저장된 리프레시 토큰과 비교
        String redisRefreshToken = redisTokenRepository.findByMemberId(memberId);
        if (redisRefreshToken == null || !redisRefreshToken.equals(reqRefreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다: " + memberId));

        // 기존 리프레시 토큰 파기 새로운 리프레시 토큰 발급 및 저장(rotation)
        String newRefreshToken = authJwtProvider.generateRefreshToken(memberId);
        redisTokenRepository.delete(memberId);
        redisTokenRepository.save(memberId, newRefreshToken);

        String accessToken = authJwtProvider.generateAccessToken(
                new AccessTokenPayload(member.getId(), member.getRole()));

        return new SignInResp(accessToken, newRefreshToken);
    }
}
