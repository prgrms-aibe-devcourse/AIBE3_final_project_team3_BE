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
    boolean existsByChatRoomIdAndChatRoomTypeAndMember_Id(Long chatRoomId, ChatMessage.chatRoomType chatRoomType, Long memberId);

    // 본인 제외 특정 대화방의 모든 멤버를 조회
    @Query("SELECT cm FROM ChatMember cm " +
           "WHERE cm.chatRoomId = :chatRoomId " +
           "AND cm.chatRoomType = :chatRoomType " +
           "AND cm.member.id <> :senderId")
    List<ChatMember> findByChatRoomIdAndChatRoomTypeAndMember_IdNot(
            @Param("chatRoomId") Long chatRoomId,
            @Param("chatRoomType") ChatMessage.chatRoomType chatRoomType,
            @Param("senderId") Long senderId);

    // 특정 대화방에서 특정 멤버를 조회
    Optional<ChatMember> findByChatRoomIdAndChatRoomTypeAndMember(Long chatRoomId, ChatMessage.chatRoomType chatRoomType, Member member);

    // 특정 대화방에서 특정 멤버 ID로 ChatMember 조회
    Optional<ChatMember> findByChatRoomIdAndChatRoomTypeAndMember_Id(Long chatRoomId, ChatMessage.chatRoomType chatRoomType, Long memberId);

    // 특정 대화방의 모든 멤버 수를 조회
    long countByChatRoomIdAndChatRoomType(Long chatRoomId, ChatMessage.chatRoomType chatRoomType);

    /**
     * 특정 멤버가 속한 모든 ChatMember 엔티티를 조회합니다.
     * @param member 멤버 엔티티
     * @return 해당 멤버가 속한 ChatMember 목록
     */
    List<ChatMember> findByMember(Member member);

    /**
     * 특정 대화방 ID와 타입에 해당하는 모든 ChatMember 엔티티를 조회합니다.
     * @param chatRoomId 대화방 ID
     * @param chatRoomType 대화방 타입
     * @return 해당 대화방의 ChatMember 목록
     */
    List<ChatMember> findByChatRoomIdAndChatRoomType(Long chatRoomId, ChatMessage.chatRoomType chatRoomType);

    /**
     * 특정 멤버가 특정 대화 타입에 속한 모든 ChatMember 엔티티를 조회합니다.
     * @param member 멤버 엔티티
     * @param chatRoomType 대화방 타입
     * @return 해당 멤버가 속한 ChatMember 목록
     */
    List<ChatMember> findByMemberAndChatRoomType(Member member, ChatMessage.chatRoomType chatRoomType);
}
