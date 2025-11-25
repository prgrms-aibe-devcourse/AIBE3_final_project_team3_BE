package triplestar.mixchat.domain.chat.chat.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.chat.chat.entity.DirectChatRoom;

import java.util.Optional;

@Repository
public interface DirectChatRoomRepository extends JpaRepository<DirectChatRoom, Long> {
    Optional<DirectChatRoom> findByUser1_IdAndUser2_Id(Long user1Id, Long user2Id);

    // Sequence 생성 시 동시성 제어용 비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM DirectChatRoom r WHERE r.id = :id")
    Optional<DirectChatRoom> findByIdWithLock(@Param("id") Long id);
}
