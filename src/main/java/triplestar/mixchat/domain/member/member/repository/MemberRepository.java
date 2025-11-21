package triplestar.mixchat.domain.member.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.dto.MemberDetailResp;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String reqEmail);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByNickname(String username);

    @Query("""
            SELECT new triplestar.mixchat.domain.member.member.dto.MemberDetailResp(
                m.id,
                m.email,
                m.name,
                m.nickname,
                m.country,
                m.englishLevel,
                m.interests,
                m.description,
                m.profileImageUrl,
            
                /* isFriend (boolean) */
                EXISTS (
                    SELECT f FROM Friendship f
                    WHERE (
                        f.smallerMember.id = :signInId AND f.largerMember.id = :memberId
                    )
                    OR (
                        f.smallerMember.id = :memberId AND f.largerMember.id = :signInId
                    )
                ),
            
                /* isRequestSent (boolean) */
                EXISTS (
                    SELECT fr FROM FriendshipRequest fr
                    WHERE fr.sender.id = :signInId
                      AND fr.receiver.id = :memberId
                ),
            
                /* receivedRequestId (Long) */
                (
                    SELECT fr.id
                    FROM FriendshipRequest fr
                    WHERE fr.sender.id = :memberId
                      AND fr.receiver.id = :signInId
                )
            )
            FROM Member m
            WHERE m.id = :memberId
    """)
    Optional<MemberDetailResp> findByIdWithFriendInfo(Long signInId, Long memberId);

    List<Member> findAllByIdIsNot(Long id);
}
