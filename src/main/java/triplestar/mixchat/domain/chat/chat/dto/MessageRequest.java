package triplestar.mixchat.domain.chat.chat.dto;

import lombok.Data;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;

// todo : record 변경, 어노테이션 재정의
@Data
public class MessageRequest {
    private Long roomId;
    private String content;
    private ChatMessage.MessageType messageType;
    private Double amount;
    private String memo;
    private Long serviceId;
}