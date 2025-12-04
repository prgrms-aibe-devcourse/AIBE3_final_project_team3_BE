package triplestar.mixchat.domain.chat.chat.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.chat.chat.entity.DirectChatRoom;

@Repository
public interface DirectChatRoomRepository extends JpaRepository<DirectChatRoom, Long> {
    Optional<DirectChatRoom> findByUser1_IdAndUser2_Id(Long user1Id, Long user2Id);

    // Sequence 생성 시 동시성 제어용 비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM DirectChatRoom r WHERE r.id = :id")
    Optional<DirectChatRoom> findByIdWithLock(@Param("id") Long id);

    // 특정 멤버가 속한 1:1 채팅방과 해당 멤버의 lastReadSequence를 한 번에 조회
    // N+1 문제 해결: user1, user2를 Fetch Join으로 즉시 로딩
    @Query("""
        SELECT r, cm.lastReadSequence
        FROM DirectChatRoom r
        JOIN ChatMember cm ON cm.chatRoomId = r.id AND cm.chatRoomType = 'DIRECT'
        JOIN FETCH r.user1
        JOIN FETCH r.user2
        WHERE cm.member.id = :memberId
        ORDER BY r.modifiedAt DESC
        """)
    List<Object[]> findRoomsAndLastReadByMemberId(@Param("memberId") Long memberId);
}
