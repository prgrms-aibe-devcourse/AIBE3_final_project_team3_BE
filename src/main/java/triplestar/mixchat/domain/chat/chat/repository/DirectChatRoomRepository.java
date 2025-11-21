package triplestar.mixchat.domain.chat.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.chat.chat.entity.DirectChatRoom; // Updated entity name

import java.util.Optional;

@Repository
public interface DirectChatRoomRepository extends JpaRepository<DirectChatRoom, Long> {
    Optional<DirectChatRoom> findByUser1_IdAndUser2_Id(Long user1Id, Long user2Id);
}
