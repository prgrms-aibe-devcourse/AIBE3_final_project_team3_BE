package triplestar.mixchat.domain.chat.find.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.auth.dto.MemberSummaryResp;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FindService {

    private final MemberRepository memberRepository;

    public List<MemberSummaryResp> findAllMembers(Long currentUserId) {
        return memberRepository.findAllByIdNot(currentUserId).stream()
                .map(MemberSummaryResp::new)
                .collect(Collectors.toList());
    }
}
