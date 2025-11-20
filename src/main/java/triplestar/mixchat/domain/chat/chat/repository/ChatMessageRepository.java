package triplestar.mixchat.domain.chat.chat.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
}