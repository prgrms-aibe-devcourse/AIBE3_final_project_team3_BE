package triplestar.mixchat.domain.member.member.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.constant.Country;
import triplestar.mixchat.domain.member.member.dto.MemberInfoModifyReq;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public void updateInfo(Long memberId, MemberInfoModifyReq req) {
        Member member = memberRepository.findById(memberId).
                orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다."));

        member.update(req.name(),
                Country.findByCode(req.country()),
                req.nickname(),
                req.englishLevel(),
                req.interest(),
                req.description());

        memberRepository.save(member);
    }
}
