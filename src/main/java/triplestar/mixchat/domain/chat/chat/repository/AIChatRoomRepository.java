package triplestar.mixchat.domain.chat.chat.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.chat.chat.entity.AIChatRoom;
import triplestar.mixchat.domain.member.member.entity.Member;

@Repository
public interface AIChatRoomRepository extends JpaRepository<AIChatRoom, Long> {
    // AI 채팅방 관련 쿼리 메서드는 추후 구현 예정

    // 특정 멤버가 속한 AI 채팅방 목록 조회
    @Query("""
            SELECT ar FROM AIChatRoom ar
            JOIN ChatMember cm ON cm.chatRoomId = ar.id
            WHERE cm.member = :member AND cm.chatRoomType = 'AI'
            """)
    List<AIChatRoom> findAllByMember(@Param("member") Member member);
}
