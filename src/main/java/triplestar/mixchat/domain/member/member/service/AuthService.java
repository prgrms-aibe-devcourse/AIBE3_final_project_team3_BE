package triplestar.mixchat.domain.member.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.constant.EnglishLevel;
import triplestar.mixchat.domain.member.member.dto.MemberJoinReq;
import triplestar.mixchat.domain.member.member.dto.MemberSummaryResp;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.entity.Password;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.global.customException.UniqueConstraintException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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
                .englishLevel(EnglishLevel.valueOf(req.englishLevel().toUpperCase()))
                .interest(req.interest())
                .description(req.description())
                .build();
    }
}
