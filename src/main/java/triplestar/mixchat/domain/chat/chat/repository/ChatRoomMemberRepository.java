package triplestar.mixchat.domain.chat.chat.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.member.member.entity.Member;

public interface ChatRoomMemberRepository extends JpaRepository<ChatMember, Long> {

    // 특정 사용자가 특정 대화방의 멤버인지 존재 여부만 빠르게 확인
    boolean existsByChatRoomIdAndChatRoomTypeAndMember_Id(Long chatRoomId, ChatRoomType chatRoomType, Long memberId);

    // 본인 제외 특정 대화방의 모든 멤버를 조회
    @Query("""
        SELECT cm FROM ChatMember cm
        WHERE cm.chatRoomId = :chatRoomId
        AND cm.chatRoomType = :chatRoomType
        AND cm.member.id <> :senderId
        """)
    List<ChatMember> findByChatRoomIdAndChatRoomTypeAndMember_IdNot(
            @Param("chatRoomId") Long chatRoomId,
            @Param("chatRoomType") ChatRoomType chatRoomType,
            @Param("senderId") Long senderId);

    // 특정 대화방에서 특정 멤버를 조회
    Optional<ChatMember> findByChatRoomIdAndChatRoomTypeAndMember(Long chatRoomId, ChatRoomType chatRoomType, Member member);

    // 특정 대화방에서 특정 멤버 ID로 ChatMember 조회
    Optional<ChatMember> findByChatRoomIdAndChatRoomTypeAndMember_Id(Long chatRoomId, ChatRoomType chatRoomType, Long memberId);

    // 특정 대화방의 모든 멤버 수를 조회
    long countByChatRoomIdAndChatRoomType(Long chatRoomId, ChatRoomType chatRoomType);

    /**
     * 특정 멤버가 속한 모든 ChatMember 엔티티를 조회합니다.
     * @param member 멤버 엔티티
     * @return 해당 멤버가 속한 ChatMember 목록
     */
    List<ChatMember> findByMember(Member member);

    // 특정 대화방 ID와 타입에 해당하는 모든 ChatMember 엔티티 조회
    List<ChatMember> findByChatRoomIdAndChatRoomType(Long chatRoomId, ChatRoomType chatRoomType);

    // 특정 대화방 ID와 타입에 해당하는 모든 ChatMember 엔티티를 멤버 정보와 함께 조회 (N+1 문제 방지)
    // @Query("SELECT cm FROM ChatMember cm JOIN FETCH cm.member m WHERE cm.chatRoomId = :chatRoomId AND cm.chatRoomType = :chatRoomType")
    // List<ChatMember> findByChatRoomIdAndChatRoomTypeWithMembers(@Param("chatRoomId") Long chatRoomId, @Param("chatRoomType") ChatRoomType chatRoomType);

    /**
     * 특정 멤버가 특정 대화 타입에 속한 모든 ChatMember 엔티티를 조회합니다.
     * @param member 멤버 엔티티
     * @param chatRoomType 대화방 타입
     * @return 해당 멤버가 속한 ChatMember 목록
     */
    List<ChatMember> findByMemberAndChatRoomType(Member member, ChatRoomType chatRoomType);

    // 방 ID 목록에 해당하는 모든 ChatMember 엔티티를 멤버 정보와 함께 조회
    @Query("""
        SELECT cm FROM ChatMember cm
        JOIN FETCH cm.member
        WHERE cm.chatRoomId IN :roomIds AND cm.chatRoomType = 'GROUP'
        """)
    List<ChatMember> findAllByRoomIdsWithMember(@Param("roomIds") List<Long> roomIds);

    // 여러 멤버의 lastReadSequence를 한 번에 일괄 쿼리로 업데이트 (Bulk Update)
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE ChatMember cm
        SET cm.lastReadSequence = :sequence, cm.lastReadAt = :now
        WHERE cm.chatRoomId = :roomId
        AND cm.chatRoomType = :chatRoomType
        AND cm.member.id IN :memberIds
        AND (cm.lastReadSequence IS NULL OR cm.lastReadSequence < :sequence)
        """)
    void bulkUpdateLastReadSequence(
        @Param("roomId") Long roomId,
        @Param("chatRoomType") ChatRoomType chatRoomType,
        @Param("memberIds") Set<Long> memberIds,
        @Param("sequence") Long sequence,
        @Param("now") LocalDateTime now
    );

    // 방 ID와 대화방 타입으로 해당 방 정보 삭제
    void deleteByChatRoomIdAndChatRoomType(Long roomId, ChatRoomType roomType);
}
