package triplestar.mixchat.domain.chat.chat.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.ai.userprompt.entity.UserPrompt;
import triplestar.mixchat.domain.chat.chat.entity.AIChatRoom;

@Repository
public interface AIChatRoomRepository extends JpaRepository<AIChatRoom, Long> {
    // AI 채팅방 관련 쿼리 메서드는 추후 구현 예정

    @Query("""
            SELECT ar.persona FROM AIChatRoom ar
            WHERE ar.id = :roomId
    """)
    Optional<UserPrompt> findRoomPersona(Long roomId);

    List<AIChatRoom> findAllByMember_Id(Long currentUserId);

    @Query("""
            SELECT ar FROM AIChatRoom ar
            JOIN FETCH ar.persona p
            WHERE ar.id =:roomId
    """)
    Optional<AIChatRoom> findByIdWithPersona(Long roomId);
}
