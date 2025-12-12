package triplestar.mixchat.domain.member.friend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.member.friend.entity.FriendshipRequest;
import triplestar.mixchat.domain.member.member.entity.Member;

@Repository
public interface FriendshipRequestRepository extends JpaRepository<FriendshipRequest, Long>, FriendshipRequestRepositoryCustom {
    boolean existsBySenderAndReceiver(Member sender, Member receiver);
}
