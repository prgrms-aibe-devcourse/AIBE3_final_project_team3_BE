package triplestar.mixchat.domain.chat.chat.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.chat.chat.entity.GroupChatRoom;

@Repository
public interface GroupChatRoomRepository extends JpaRepository<GroupChatRoom, Long> {
    // 특정 멤버가 속한 그룹 채팅방 목록 조회
    @Query("SELECT DISTINCT gcr FROM GroupChatRoom gcr " +
           "JOIN ChatMember cm ON cm.chatRoomId = gcr.id " +
           "WHERE cm.member.id = :memberId AND cm.chatRoomType = 'GROUP'")
    List<GroupChatRoom> findAllByMemberId(@Param("memberId") Long memberId);

    // 특정 멤버가 속하지 않은 공개 그룹 채팅방 목록 조회
    @Query("SELECT gcr FROM GroupChatRoom gcr " +
           "WHERE NOT EXISTS (" +
           "  SELECT 1 FROM ChatMember cm " +
           "  WHERE cm.chatRoomId = gcr.id AND cm.member.id = :memberId AND cm.chatRoomType = 'GROUP'" +
           ")")
    List<GroupChatRoom> findPublicRoomsExcludingMemberId(@Param("memberId") Long memberId);
}

