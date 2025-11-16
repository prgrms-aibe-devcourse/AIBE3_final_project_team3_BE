package triplestar.mixchat.domain.chat.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

    /**
     * 특정 사용자가 특정 채팅방의 멤버인지 존재 여부만 빠르게 확인합니다.
     * @param memberId 사용자 ID
     * @param chatRoomId 채팅방 ID
     * @return 멤버이면 true, 아니면 false
     */
    boolean existsByMemberIdAndChatRoomId(Long memberId, Long chatRoomId);
}
