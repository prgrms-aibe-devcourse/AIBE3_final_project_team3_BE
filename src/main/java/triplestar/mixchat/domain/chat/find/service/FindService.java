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

    // todo: 페이징을 통해 db에서 단위 별로 가져오기 필수
    public List<MemberSummaryResp> findAllMembers(Long currentUserId) {
        return memberRepository.findAllByIdIsNot(currentUserId).stream()
                .map(MemberSummaryResp::new)
                .collect(Collectors.toList());
    }
}
