package triplestar.mixchat.domain.chat.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MessageResponse {
    private String id;
    private Long senderId;
    private String sender;
    private String content;
    private LocalDateTime createdAt;
    private ChatMessage.MessageType messageType;
    private Double amount;
    private String memo;
    private Long serviceId;

    // todo : builder 패턴 피하고, record로 통일
    public static MessageResponse from(ChatMessage entity, String senderName) {
        return MessageResponse.builder()
                .id(entity.getId())
                .senderId(entity.getSenderId())
                .sender(senderName)
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .messageType(entity.getMessageType())
                .build();
    }
}