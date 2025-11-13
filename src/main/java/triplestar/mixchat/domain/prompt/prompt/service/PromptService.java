package triplestar.mixchat.domain.prompt.prompt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.member.member.constant.MembershipGrade;
import triplestar.mixchat.global.security.CustomUserDetails;
import triplestar.mixchat.domain.prompt.prompt.constant.PromptType;
import triplestar.mixchat.domain.prompt.prompt.dto.PromptReq;
import triplestar.mixchat.domain.prompt.prompt.dto.PromptListResp;
import triplestar.mixchat.domain.prompt.prompt.dto.PromptDetailResp;
import triplestar.mixchat.domain.prompt.prompt.entity.Prompt;
import triplestar.mixchat.domain.prompt.prompt.repository.PromptRepository;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromptService {
    private final PromptRepository promptRepository;
    private final MemberRepository memberRepository;

    // 현재 로그인한 멤버 조회
    public Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new IllegalStateException();
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return memberRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalStateException());
    }

    // 프리미엄 등급 확인
    private void checkPremium(Member member) {
        if (member.getMembershipGrade() != MembershipGrade.PREMIUM) {
            throw new AccessDeniedException("프리미엄 등급이 아닙니다.");
        }
    }

    // 프롬프트 작성 (PREMIUM 유저)
    @Transactional
    public PromptDetailResp create(PromptReq req) {
        Member member = getCurrentMember();
        checkPremium(member);
        Prompt prompt = Prompt.create(member, req.title(), req.content(), req.promptType());
        Prompt saved = promptRepository.save(prompt);
        return new PromptDetailResp(saved);
    }

    // 본인의 프롬프트만 수정 (PREMIUM 유저)
    @Transactional
    public PromptDetailResp update(Long id, PromptReq req) {
        Member member = getCurrentMember();
        checkPremium(member);
        Prompt prompt = promptRepository.findById(id).orElseThrow(IllegalStateException::new);
        if (prompt.getMember() == null || !prompt.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("본인 프롬프트가 아닙니다.");
        }
        prompt.modify(req.title(), req.content(), req.promptType());
        return new PromptDetailResp(prompt);
    }

    // 본인의 프롬프트만 삭제 (PREMIUM 유저)
    @Transactional
    public void delete(Long id) {
        Member member = getCurrentMember();
        checkPremium(member);
        Prompt prompt = promptRepository.findById(id).orElseThrow(IllegalStateException::new);
        if (prompt.getMember() == null || !prompt.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("본인 프롬프트가 아닙니다.");
        }
        promptRepository.deleteById(id);
    }

    // 모든 유저가 미리 작성된 프롬프트를 조회, PREMIUM 유저는 본인 프롬프트도 조회
    @Transactional(readOnly = true)
    public List<PromptListResp> list() {
        Member member = getCurrentMember();
        MembershipGrade grade = member.getMembershipGrade();
        List<Prompt> prompts;
        if (grade == MembershipGrade.PREMIUM) {
            List<Prompt> preScripted = promptRepository.findByType(PromptType.PRE_SCRIPTED);
            List<Prompt> custom = promptRepository.findByTypeAndMember_Id(PromptType.CUSTOM, member.getId());
            prompts = new java.util.ArrayList<>();
            prompts.addAll(preScripted);
            prompts.addAll(custom);
        } else {
            prompts = promptRepository.findByType(PromptType.PRE_SCRIPTED);
        }
        return prompts.stream().map(PromptListResp::new).collect(Collectors.toList());
    }

    // 프롬프트 상세 조회 (PREMIUM 유저만, 본인 CUSTOM 프롬프트만)
    @Transactional(readOnly = true)
    public PromptDetailResp detail(Long id) {
        Member member = getCurrentMember();
        if (member.getMembershipGrade() != MembershipGrade.PREMIUM) {
            throw new AccessDeniedException("프리미엄 등급이 아닙니다.");
        }
        Prompt prompt = promptRepository.findById(id).orElseThrow(IllegalStateException::new);
        if (prompt.getType() != PromptType.CUSTOM || prompt.getMember() == null || !prompt.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("본인 프롬프트가 아닙니다.");
        }
        return new PromptDetailResp(prompt);
    }
}
