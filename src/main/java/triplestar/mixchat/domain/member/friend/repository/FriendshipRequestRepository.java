package triplestar.mixchat.domain.member.friend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.member.friend.entity.FriendshipRequest;

@Repository
public interface FriendshipRequestRepository extends JpaRepository<FriendshipRequest, Long> {
}
