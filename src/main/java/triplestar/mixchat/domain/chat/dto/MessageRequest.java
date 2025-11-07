package triplestar.mixchat.domain.chat.dto;

import lombok.Data;
import triplestar.mixchat.domain.chat.entity.ChatMessage;

@Data
public class MessageRequest {
    private Long roomId;
    private String content;
    private ChatMessage.MessageType messageType;
    private Double amount;
    private String memo;
    private Long serviceId;
}