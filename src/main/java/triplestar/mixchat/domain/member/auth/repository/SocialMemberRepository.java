package triplestar.mixchat.domain.member.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.member.auth.entity.SocialMember;

@Repository
public interface SocialMemberRepository extends JpaRepository<SocialMember, Long> {
}
