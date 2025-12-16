package triplestar.mixchat.domain.chat.chat.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.chat.chat.entity.GroupChatRoom;

@Repository
public interface GroupChatRoomRepository extends JpaRepository<GroupChatRoom, Long> {
    // 특정 멤버가 속한 그룹 채팅방 목록 조회
    @Query("""
        SELECT DISTINCT gcr FROM GroupChatRoom gcr
        JOIN ChatMember cm ON cm.chatRoomId = gcr.id
        WHERE cm.member.id = :memberId AND cm.chatRoomType = 'GROUP'
        ORDER BY gcr.modifiedAt DESC
        """)
    List<GroupChatRoom> findAllByMemberId(@Param("memberId") Long memberId);

    // 특정 멤버가 속하지 않은 그룹 채팅방 목록 조회 (비밀번호 있는 방 포함)
    @Query("""
        SELECT gcr FROM GroupChatRoom gcr
        WHERE NOT EXISTS (
            SELECT 1 FROM ChatMember cm
            WHERE cm.chatRoomId = gcr.id AND cm.member.id = :memberId AND cm.chatRoomType = 'GROUP'
        )
        """)
    Page<GroupChatRoom> findPublicRoomsExcludingMemberId(@Param("memberId") Long memberId, Pageable pageable);

    // Sequence 생성 시 동시성 제어용 비관적 락, 서비스에 @Transactional 필수
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM GroupChatRoom r WHERE r.id = :id")
    Optional<GroupChatRoom> findByIdWithLock(@Param("id") Long id);

    // Load Test Cleanup: [LOAD_TEST] 태그가 있는 그룹 채팅방 조회
    @Query("""
        SELECT r FROM GroupChatRoom r
        WHERE r.name LIKE '[LOAD_TEST]%' OR r.topic = 'LOAD_TEST'
        """)
    List<GroupChatRoom> findLoadTestRooms();
}
