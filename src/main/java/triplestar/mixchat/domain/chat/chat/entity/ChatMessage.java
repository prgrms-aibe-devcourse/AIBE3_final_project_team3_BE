package triplestar.mixchat.domain.chat.chat.entity;

import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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

    // 대화 타입 (1:1, 그룹, AI 등)을 구분하는 enum 추가
    public enum chatRoomType {
        DIRECT, GROUP, AI
    }

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    private chatRoomType chatRoomType; // 대화 타입을 저장하는 필드 추가

    public ChatMessage(Long chatRoomId, Long senderId, String content, MessageType messageType, chatRoomType chatRoomType) {
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
        if (chatRoomType == null) {
            throw new IllegalArgumentException("chatRoomType은 null일 수 없습니다.");
        }

        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.messageType = messageType;
        this.chatRoomType = chatRoomType; // 필드 할당
    }
}

//대량 insert 시 Bulk Write 사용?
//TTL 컬렉션 사용하면 자동 삭제 가능?
//검색시 text index또는 elastic search 연동 고려
