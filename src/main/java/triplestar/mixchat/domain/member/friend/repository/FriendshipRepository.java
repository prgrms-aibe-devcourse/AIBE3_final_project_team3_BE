package triplestar.mixchat.domain.member.friend.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.member.friend.dto.FriendDetailResp;
import triplestar.mixchat.domain.member.friend.entity.Friendship;
import triplestar.mixchat.domain.member.member.entity.Member;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    boolean existsBySmallerMember_IdAndLargerMember_Id(Long smallerId, Long largerId);

    Optional<Friendship> findBySmallerMember_IdAndLargerMember_Id(Long smallerId, Long largerId);

    // 특정 멤버의 친구 id 목록 조회
    @Query("""
                SELECT m
                FROM Friendship f
                JOIN Member m
                    ON m.id = (
                        CASE
                            WHEN f.smallerMember.id = :memberId THEN f.largerMember.id
                            ELSE f.smallerMember.id
                        END
                    )
                WHERE :memberId IN (f.smallerMember.id, f.largerMember.id)
            """)
    Page<Member> findFriendsByMemberId(Long memberId, Pageable pageable);

    @Query("""
                SELECT new triplestar.mixchat.domain.member.friend.dto.FriendDetailResp(
                    m.id,
                    m.nickname,
                    CONCAT('', m.country),
                    CONCAT('', m.englishLevel),
                    m.interests,
                    m.description,
                    m.profileImageUrl,
                    f.createdAt
                )
                FROM Friendship f
                JOIN Member m ON (
                    (m.id = :friendId)
                    AND (
                        (f.smallerMember.id = :smallerId AND f.largerMember.id = :largerId)
                    )
                )
            """)
    FriendDetailResp findFriendDetail(Long smallerId, Long largerId, Long friendId);
}
