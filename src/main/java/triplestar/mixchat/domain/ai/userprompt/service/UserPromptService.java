package triplestar.mixchat.domain.ai.userprompt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.ai.userprompt.entity.UserPrompt;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.member.member.constant.MembershipGrade;
import triplestar.mixchat.domain.ai.userprompt.constant.UserPromptType;
import triplestar.mixchat.domain.ai.userprompt.dto.UserPromptReq;
import triplestar.mixchat.domain.ai.userprompt.dto.UserPromptResp;
import triplestar.mixchat.domain.ai.userprompt.dto.UserPromptDetailResp;
import triplestar.mixchat.domain.ai.userprompt.repository.UserPromptRepository;

import org.springframework.security.access.AccessDeniedException;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPromptService {
    private final UserPromptRepository userPromptRepository;
    private final MemberRepository memberRepository;

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("멤버를 찾을 수 없습니다."));
    }

    private void checkPremium(Member member) {
        if (!member.isPremium()) {
            throw new AccessDeniedException("프리미엄 등급이 아닙니다.");
        }
    }

    @Transactional
    public UserPromptDetailResp create(Long memberId, UserPromptReq req) {
        Member member = getMember(memberId);
        checkPremium(member);
        UserPrompt userPrompt = UserPrompt.create(member, req.title(), req.content(), req.promptType());
        UserPrompt saved = userPromptRepository.save(userPrompt);
        return new UserPromptDetailResp(saved);
    }

    private UserPrompt getPrompt(Long id) {
        return userPromptRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public void update(Long memberId, Long id, UserPromptReq req) {
        Member member = getMember(memberId);
        checkPremium(member);
        UserPrompt userPrompt = getPrompt(id);
        if (userPrompt.isDefaultPrompt() || !userPrompt.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("본인 프롬프트가 아닙니다.");
        }
        userPrompt.modify(req.title(), req.content(), req.promptType());
    }

    @Transactional
    public void delete(Long memberId, Long id) {
        Member member = getMember(memberId);
        checkPremium(member);
        UserPrompt userPrompt = getPrompt(id);
        if (userPrompt.getMember() == null || !userPrompt.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("본인 프롬프트가 아닙니다.");
        }
        userPromptRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UserPromptResp> list(Long memberId) {
        Member member = getMember(memberId);
        MembershipGrade grade = member.getMembershipGrade();
        List<UserPrompt> userPrompts;
        if (grade == MembershipGrade.PREMIUM) {
            userPrompts = userPromptRepository.findForPremium(UserPromptType.PRE_SCRIPTED, UserPromptType.CUSTOM, member.getId());
        } else {
            userPrompts = userPromptRepository.findByPromptType(UserPromptType.PRE_SCRIPTED);
        }
        return userPrompts.stream().map(UserPromptResp::new).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserPromptDetailResp detail(Long memberId, Long id) {
        Member member = getMember(memberId);
        if (member.getMembershipGrade() != MembershipGrade.PREMIUM) {
            throw new AccessDeniedException("프리미엄 등급이 아닙니다.");
        }
        UserPrompt userPrompt = getPrompt(id);
        if (userPrompt.getPromptType() != UserPromptType.CUSTOM || userPrompt.getMember() == null || !userPrompt.getMember().getId().equals(member.getId())) {
            throw new AccessDeniedException("본인 프롬프트가 아닙니다.");
        }
        return new UserPromptDetailResp(userPrompt);
    }
}
