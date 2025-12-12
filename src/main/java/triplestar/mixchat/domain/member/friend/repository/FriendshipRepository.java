package triplestar.mixchat.domain.member.friend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.member.friend.dto.FriendDetailResp;
import triplestar.mixchat.domain.member.friend.dto.FriendshipStateInfo;
import triplestar.mixchat.domain.member.friend.entity.Friendship;
import triplestar.mixchat.domain.member.member.entity.Member;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long>, FriendshipRepositoryCustom {
    boolean existsBySmallerMember_IdAndLargerMember_Id(Long smallerId, Long largerId);

    Optional<Friendship> findBySmallerMember_IdAndLargerMember_Id(Long smallerId, Long largerId);
}