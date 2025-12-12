package triplestar.mixchat.domain.member.member.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import triplestar.mixchat.domain.member.member.entity.Member;

public interface MemberRepositoryCustom {

    Page<Member> findAllByIdIsNot(Long id, Pageable pageable);

    Page<Member> findByIds(Long currentUserId, List<Long> onlineMemberIds, Pageable pageable);

    void updateLastSeenAt(Long memberId, LocalDateTime lastSeenAt);
}
