package triplestar.mixchat.domain.chat.chat.entity;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;

//mongoDB용 Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "chat_messages")
@CompoundIndex(name = "idx_room_type_sequence", def = "{'chatRoomId': 1, 'chatRoomType': 1, 'sequence': 1}")
public class ChatMessage {
    @Id
    private String id; //mongoDB용 ID

    private Long chatRoomId; // MySQL ChatRoom 참조 ID
    private Long senderId;   // MySQL Member 참조 ID
    private Long sequence;   // 채팅방 내 메시지 순서 번호

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

    private ChatRoomType chatRoomType;

    public ChatMessage(Long chatRoomId, Long senderId, Long sequence, String content, MessageType messageType, ChatRoomType chatRoomType) {
        if (chatRoomId == null) {
            throw new IllegalArgumentException("chatRoomId는 null일 수 없습니다.");
        }
        if (senderId == null) {
            throw new IllegalArgumentException("senderId는 null일 수 없습니다.");
        }
        if (sequence == null) {
            throw new IllegalArgumentException("sequence는 null일 수 없습니다.");
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
        this.sequence = sequence;
        this.content = content;
        this.messageType = messageType;
        this.chatRoomType = chatRoomType;
    }
}

//대량 insert 시 Bulk Write 사용?
//TTL 컬렉션 사용하면 자동 삭제 가능?
//검색시 text index또는 elastic search 연동 고려
