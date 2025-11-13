package triplestar.mixchat.domain.member.friend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.friend.entity.Friendship;
import triplestar.mixchat.domain.member.friend.repository.FriendshipRepository;
import triplestar.mixchat.domain.member.member.entity.Member;

// 친구 관계 요청 검증은 FriendshipRequestService가 처리
@Service
@Transactional
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;

    public boolean isFriends(Long memberId1, Long memberId2) {
        Long smallerId = Math.min(memberId1, memberId2);
        Long largerId = Math.max(memberId1, memberId2);

        return friendshipRepository.existsBySmallerMember_IdAndLargerMember_Id(smallerId, largerId);
    }

    public void createFriendship(Member member1, Member member2) {
        Member smallerMember = member1.getId() < member2.getId() ? member1 : member2;
        Member largerMember = member1.getId() < member2.getId() ? member2 : member1;

        Friendship friendship = new Friendship(smallerMember, largerMember);
        friendshipRepository.save(friendship);
    }

    public void deleteFriendship(Long id1, Long id2) {
        // memberId에 대한 유효성 검사는 친구관계 성립시 이미 처리되었으므로 생략
        Long smallerId = Math.min(id1, id2);
        Long largerId = Math.max(id1, id2);

        Friendship friendship = friendshipRepository.findBySmallerMember_IdAndLargerMember_Id(smallerId, largerId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 친구 관계입니다"));

        friendshipRepository.delete(friendship);
    }
}
