package triplestar.mixchat.domain.chat.chat.repository;


import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.chat.chat.entity.ChatRoom;
import triplestar.mixchat.domain.member.member.entity.Member;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // querydsl 로 변경 예정
    // nativeQuery 라서 table 어미에 s추가 했습니다.
    @Query(value = """
    SELECT cr.*
    FROM chat_rooms cr
    WHERE cr.room_type = 'DIRECT'
      AND EXISTS (
          SELECT 1 FROM chat_members cm 
          WHERE cm.chat_room_id = cr.id AND cm.member_id = :#{#member1.id}
      )
      AND EXISTS (
          SELECT 1 FROM chat_members cm 
          WHERE cm.chat_room_id = cr.id AND cm.member_id = :#{#member2.id}
      )
    """, nativeQuery = true)
    Optional<ChatRoom> findDirectRoomByMembers(@Param("member1") Member member1, @Param("member2") Member member2);

    @Query("SELECT cr FROM ChatRoom cr JOIN cr.members cm WHERE cm.member = :member")
    List<ChatRoom> findAllByMember(@Param("member") Member member);
}
