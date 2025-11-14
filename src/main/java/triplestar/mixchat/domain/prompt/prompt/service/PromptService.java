package triplestar.mixchat.domain.prompt.prompt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.member.member.constant.MembershipGrade;
import triplestar.mixchat.domain.prompt.prompt.constant.PromptType;
import triplestar.mixchat.domain.prompt.prompt.dto.PromptReq;
import triplestar.mixchat.domain.prompt.prompt.dto.PromptListResp;
import triplestar.mixchat.domain.prompt.prompt.dto.PromptDetailResp;
import triplestar.mixchat.domain.prompt.prompt.entity.Prompt;
import triplestar.mixchat.domain.prompt.prompt.repository.PromptRepository;

import org.springframework.security.access.AccessDeniedException;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptService {
    private final PromptRepository promptRepository;
    private final MemberRepository memberRepository;

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("멤버를 찾을 수 없습니다."));
    }

    private void checkPremium(Member member) {
        if (!member.isPremium()) {
            throw new AccessDeniedException("프리미엄 등급이 아닙니다.");
        }
    }

    @Transactional
    public PromptDetailResp create(Long memberId, PromptReq req) {
        Member member = getMember(memberId);
        checkPremium(member);
        Prompt prompt = Prompt.create(member, req.title(), req.content(), req.promptType());
        Prompt saved = promptRepository.save(prompt);
        return new PromptDetailResp(saved);
    }

    private Prompt getPrompt(Long id) {
        return promptRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public void update(Long memberId, Long id, PromptReq req) {
        Member member = getMember(memberId);
        checkPremium(member);
        Prompt prompt = getPrompt(id);
        if (prompt.isDefaultPrompt() || !prompt.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("본인 프롬프트가 아닙니다.");
        }
        prompt.modify(req.title(), req.content(), req.promptType());
    }

    @Transactional
    public void delete(Long memberId, Long id) {
        Member member = getMember(memberId);
        checkPremium(member);
        Prompt prompt = getPrompt(id);
        if (prompt.getMember() == null || !prompt.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("본인 프롬프트가 아닙니다.");
        }
        promptRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<PromptListResp> list(Long memberId) {
        Member member = getMember(memberId);
        MembershipGrade grade = member.getMembershipGrade();
        List<Prompt> prompts;
        if (grade == MembershipGrade.PREMIUM) {
            prompts = promptRepository.findForPremium(PromptType.PRE_SCRIPTED, PromptType.CUSTOM, member.getId());
        } else {
            prompts = promptRepository.findByType(PromptType.PRE_SCRIPTED);
        }
        return prompts.stream().map(PromptListResp::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PromptDetailResp detail(Long memberId, Long id) {
        Member member = getMember(memberId);
        if (member.getMembershipGrade() != MembershipGrade.PREMIUM) {
            throw new AccessDeniedException("프리미엄 등급이 아닙니다.");
        }
        Prompt prompt = getPrompt(id);
        if (prompt.getType() != PromptType.CUSTOM || prompt.getMember() == null || !prompt.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("본인 프롬프트가 아닙니다.");
        }
        return new PromptDetailResp(prompt);
    }
}
