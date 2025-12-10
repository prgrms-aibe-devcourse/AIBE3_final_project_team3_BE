package triplestar.mixchat.domain.chat.chat.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    // 기존: 전체 메시지 조회 (하위 호환성 유지)
    List<ChatMessage> findByChatRoomIdAndChatRoomTypeOrderByCreatedAtAsc(Long chatRoomId, ChatRoomType chatRoomType);

    // 페이징: 최신 메시지부터 N개 (sequence 내림차순)
    List<ChatMessage> findByChatRoomIdAndChatRoomTypeOrderBySequenceDesc(
        Long chatRoomId,
        ChatRoomType chatRoomType,
        Pageable pageable
    );

    // 페이징: cursor 이전 메시지 N개 (sequence < cursor, 내림차순)
    List<ChatMessage> findByChatRoomIdAndChatRoomTypeAndSequenceLessThanOrderBySequenceDesc(
        Long chatRoomId,
        ChatRoomType chatRoomType,
        Long sequence,
        Pageable pageable
    );

    // [추가] 페이징 + 입장 시간 필터: 최신 메시지부터 N개
    List<ChatMessage> findByChatRoomIdAndChatRoomTypeAndCreatedAtGreaterThanEqualOrderBySequenceDesc(
            Long chatRoomId,
            ChatRoomType chatRoomType,
            LocalDateTime joinDate,
            Pageable pageable
    );

    // [추가] 페이징 + 입장 시간 필터: cursor 이전 메시지 N개
    List<ChatMessage> findByChatRoomIdAndChatRoomTypeAndSequenceLessThanAndCreatedAtGreaterThanEqualOrderBySequenceDesc(
            Long chatRoomId,
            ChatRoomType chatRoomType,
            Long sequence,
            LocalDateTime joinDate,
            Pageable pageable
    );

    // 특정 채팅방의 최신 메시지 1개 조회
    Optional<ChatMessage> findTopByChatRoomIdAndChatRoomTypeOrderBySequenceDesc(
            Long chatRoomId,
            ChatRoomType chatRoomType
    );

    List<ChatMessage> findByChatRoomIdAndChatRoomTypeOrderByCreatedAtDescIdDesc(
            Long roomId,
            ChatRoomType chatRoomType,
            Pageable pageable
    );

    // Load Test Cleanup: 특정 채팅방의 모든 메시지 삭제 (MongoDB)
    void deleteByChatRoomIdAndChatRoomType(Long chatRoomId, ChatRoomType chatRoomType);

    // Load Test Cleanup: 삭제 전 카운트 확인용
    long countByChatRoomIdAndChatRoomType(Long chatRoomId, ChatRoomType chatRoomType);
}