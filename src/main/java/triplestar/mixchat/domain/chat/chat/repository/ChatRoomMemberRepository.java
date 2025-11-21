package triplestar.mixchat.domain.chat.chat.repository;

import java.util.List;
import java.util.Optional; // Optional import 추가
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.member.member.entity.Member; // Member import 추가

public interface ChatRoomMemberRepository extends JpaRepository<ChatMember, Long> {

    // 특정 사용자가 특정 대화방의 멤버인지 존재 여부만 빠르게 확인
    boolean existsByChatRoomIdAndConversationTypeAndMember_Id(Long chatRoomId, ChatMessage.ConversationType conversationType, Long memberId);

    // 본인 제외 특정 대화방의 모든 멤버를 조회
    @Query("SELECT cm FROM ChatMember cm " +
           "WHERE cm.chatRoomId = :chatRoomId " +
           "AND cm.conversationType = :conversationType " +
           "AND cm.member.id <> :senderId")
    List<ChatMember> findByChatRoomIdAndConversationTypeAndMember_IdNot(
            @Param("chatRoomId") Long chatRoomId,
            @Param("conversationType") ChatMessage.ConversationType conversationType,
            @Param("senderId") Long senderId);

    // 특정 대화방에서 특정 멤버를 조회
    Optional<ChatMember> findByChatRoomIdAndConversationTypeAndMember(Long chatRoomId, ChatMessage.ConversationType conversationType, Member member);

    // 특정 대화방의 모든 멤버 수를 조회
    long countByChatRoomIdAndConversationType(Long chatRoomId, ChatMessage.ConversationType conversationType);

    /**
     * 특정 멤버가 속한 모든 ChatMember 엔티티를 조회합니다.
     * @param member 멤버 엔티티
     * @return 해당 멤버가 속한 ChatMember 목록
     */
    List<ChatMember> findByMember(Member member);

    /**
     * 특정 대화방 ID와 타입에 해당하는 모든 ChatMember 엔티티를 조회합니다.
     * @param chatRoomId 대화방 ID
     * @param conversationType 대화방 타입
     * @return 해당 대화방의 ChatMember 목록
     */
    List<ChatMember> findByChatRoomIdAndConversationType(Long chatRoomId, ChatMessage.ConversationType conversationType);

    /**
     * 특정 멤버가 특정 대화 타입에 속한 모든 ChatMember 엔티티를 조회합니다.
     * @param member 멤버 엔티티
     * @param conversationType 대화방 타입
     * @return 해당 멤버가 속한 ChatMember 목록
     */
    List<ChatMember> findByMemberAndConversationType(Member member, ChatMessage.ConversationType conversationType);
}
