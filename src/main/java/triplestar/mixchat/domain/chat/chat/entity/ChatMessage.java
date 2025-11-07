package triplestar.mixchat.domain.chat.chat.entity;

import jakarta.persistence.Id;
import lombok.*;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

//mongoDB용 Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@Document(collection = "chat_messages")
public class ChatMessage extends BaseEntity {
    @Id
    private String id; //mongoDB용 ID

    private Long chatRoomId; // MySQL ChatRoom 참조 ID
    private Long senderId;   // MySQL Member 참조 ID

    private String content;
    private MessageType messageType;

    public enum MessageType {
        TEXT, IMAGE, FILE, SYSTEM
    }
}

//대량 insert 시 Bulk Write 사용?
//TTL 컬렉션 사용하면 자동 삭제 가능?
//검색시 text index또는 elastic search 연동 고려