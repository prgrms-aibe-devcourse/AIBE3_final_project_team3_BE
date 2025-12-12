package triplestar.mixchat.domain.member.friend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import triplestar.mixchat.domain.member.friend.dto.FriendDetailResp;
import triplestar.mixchat.domain.member.friend.dto.FriendshipStateInfo;
import triplestar.mixchat.domain.member.member.entity.Member;

public interface FriendshipRepositoryCustom {

    Page<Member> findFriendsByMemberId(Long memberId, Pageable pageable);

    FriendDetailResp findFriendDetail(Long smallerId, Long largerId, Long friendId);

    FriendshipStateInfo findFriendshipStateInfo(Long loginId, Long memberId);
}
