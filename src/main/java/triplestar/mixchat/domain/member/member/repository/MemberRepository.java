package triplestar.mixchat.domain.member.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.member.member.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
}
