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
        if (chatRoomId == null) {
            throw new IllegalArgumentException("chatRoomId는 null일 수 없습니다.");
        }
        if (senderId == null) {
            throw new IllegalArgumentException("senderId는 null일 수 없습니다.");
        }
        if (messageType == null) {
            throw new IllegalArgumentException("messageType은 null일 수 없습니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("content는 비어 있을 수 없습니다.");
        }

        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.messageType = messageType;
    }
}

//대량 insert 시 Bulk Write 사용?
//TTL 컬렉션 사용하면 자동 삭제 가능?
//검색시 text index또는 elastic search 연동 고려
