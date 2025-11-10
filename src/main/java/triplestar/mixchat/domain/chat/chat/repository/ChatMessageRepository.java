package triplestar.mixchat.domain.chat.chat.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
    }