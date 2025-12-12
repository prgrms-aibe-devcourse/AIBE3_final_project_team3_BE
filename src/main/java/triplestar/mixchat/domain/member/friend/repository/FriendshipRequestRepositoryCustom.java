package triplestar.mixchat.domain.member.friend.repository;

import java.util.Optional;
import triplestar.mixchat.domain.member.friend.entity.FriendshipRequest;

public interface FriendshipRequestRepositoryCustom {
    Optional<FriendshipRequest> findByIdWithSenderReceiver(Long id);
}
