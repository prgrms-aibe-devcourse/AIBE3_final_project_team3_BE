package triplestar.mixchat.domain.member.friend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.friend.dto.FriendDetailResp;
import triplestar.mixchat.domain.member.friend.dto.FriendSummaryResp;
import triplestar.mixchat.domain.member.friend.entity.Friendship;
import triplestar.mixchat.domain.member.friend.repository.FriendshipRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;

// 친구 관계 요청 검증은 FriendshipRequestService가 처리
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final MemberRepository memberRepository;

    public boolean isFriends(Long memberId1, Long memberId2) {
        Long smallerId = Math.min(memberId1, memberId2);
        Long largerId = Math.max(memberId1, memberId2);

        return friendshipRepository.existsBySmallerMember_IdAndLargerMember_Id(smallerId, largerId);
    }

    // 친구 요청 수락시 호출되므로 조회한 member 엔티티를 파라미터로 받음
    @Transactional
    public void createFriendship(Member member1, Member member2) {
        Member smallerMember = member1.getId() < member2.getId() ? member1 : member2;
        Member largerMember = member1.getId() < member2.getId() ? member2 : member1;

        Friendship friendship = new Friendship(smallerMember, largerMember);
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void deleteFriendship(Long memberId1, Long memberId2) {
        // memberId에 대한 유효성 검사는 친구관계 성립시 이미 처리되었으므로 생략
        Long smallerId = Math.min(memberId1, memberId2);
        Long largerId = Math.max(memberId1, memberId2);

        Friendship friendship = friendshipRepository.findBySmallerMember_IdAndLargerMember_Id(smallerId, largerId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 친구 관계입니다"));

        friendshipRepository.delete(friendship);
    }

    public Page<FriendSummaryResp> getFriends(Long currentMemberId, Pageable pageable) {
        Page<Long> friendIds = friendshipRepository.findByMemberId(currentMemberId, pageable);
        Page<Member> friends = memberRepository.findAllByIdIn(friendIds.getContent(), pageable);

        return friends.map(FriendSummaryResp::from);
    }

    public FriendDetailResp getFriend(Long currentMemberId, Long friendId) {
        if (!isFriends(currentMemberId, friendId)) {
            throw new EntityNotFoundException("존재하지 않는 친구 관계입니다.");
        }

        Long smallerId = Math.min(currentMemberId, friendId);
        Long largerId = Math.max(currentMemberId, friendId);

        return friendshipRepository.findFriendDetail(smallerId, largerId, friendId);
    }
}
