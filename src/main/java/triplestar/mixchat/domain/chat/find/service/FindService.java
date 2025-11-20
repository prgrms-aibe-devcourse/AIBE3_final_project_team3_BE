package triplestar.mixchat.domain.chat.find.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.dto.MemberSummaryResp;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FindService {

    private final MemberRepository memberRepository;

    // todo: 페이징을 통해 db에서 단위 별로 가져오기 필수
    public List<MemberSummaryResp> findAllMembers(Long currentUserId) {
        log.info("FindService: Fetching all members excluding ID: {}", currentUserId); // 로그 추가
        List<Member> members = memberRepository.findAllByIdIsNot(currentUserId);
        log.info("FindService: Found {} members excluding ID: {}", members.size(), currentUserId); // 로그 추가
        return members.stream()
                .map(MemberSummaryResp::new)
                .collect(Collectors.toList());
    }
}
