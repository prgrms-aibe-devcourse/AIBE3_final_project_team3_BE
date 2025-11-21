package triplestar.mixchat.domain.chat.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.chat.chat.entity.GroupChatRoom;
import triplestar.mixchat.domain.member.member.entity.Member; // Member import 추가

import java.util.List; // List import 추가

@Repository
public interface GroupChatRoomRepository extends JpaRepository<GroupChatRoom, Long> {
    // 특정 멤버가 속한 그룹 채팅방 목록 조회
    @Query("SELECT gcr FROM GroupChatRoom gcr JOIN ChatMember cm WHERE cm.chatRoomId = gcr.id AND cm.member = :member AND cm.conversationType = 'GROUP'")
    List<GroupChatRoom> findAllByMember(@Param("member") Member member);
}

