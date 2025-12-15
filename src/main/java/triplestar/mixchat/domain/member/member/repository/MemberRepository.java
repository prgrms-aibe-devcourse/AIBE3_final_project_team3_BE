package triplestar.mixchat.domain.member.member.repository;

import java.lang.ScopedValue;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.member.member.constant.Role;
import triplestar.mixchat.domain.member.member.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    boolean existsByEmail(String reqEmail);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String username);

    Page<Member> findAllByIdIn(List<Long> ids, Pageable pageable);

    Optional<Member> findByRole(Role role);
}
