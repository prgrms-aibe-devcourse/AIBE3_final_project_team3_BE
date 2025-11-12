package triplestar.mixchat.domain.chat.chat.entity;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Objects;

//mongoDB용 Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "chat_messages")
public class ChatMessage {
    @Id
    private String id; //mongoDB용 ID

    private Long chatRoomId; // MySQL ChatRoom 참조 ID
    private Long senderId;   // MySQL Member 참조 ID

    private String content;
    private MessageType messageType;

    public enum MessageType {
        TEXT, IMAGE, FILE, SYSTEM
    }

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public ChatMessage(Long chatRoomId, Long senderId, String content, MessageType messageType) {
        Objects.requireNonNull(chatRoomId, "chatRoomId must not be null");
        Objects.requireNonNull(senderId, "senderId must not be null");
        Objects.requireNonNull(messageType, "messageType must not be null");

        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.messageType = messageType;
    }
}

//대량 insert 시 Bulk Write 사용?
//TTL 컬렉션 사용하면 자동 삭제 가능?
//검색시 text index또는 elastic search 연동 고려
