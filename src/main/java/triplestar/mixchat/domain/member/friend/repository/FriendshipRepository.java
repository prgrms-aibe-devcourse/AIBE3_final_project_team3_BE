package triplestar.mixchat.domain.member.friend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.member.friend.entity.Friendship;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    boolean existsBySmallerMember_IdAndLargerMember_Id(Long smallerId, Long largerId);

    Optional<Friendship> findBySmallerMember_IdAndLargerMember_Id(Long smallerId, Long largerId);
}
