package triplestar.mixchat.domain.chat.chat.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;

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
}