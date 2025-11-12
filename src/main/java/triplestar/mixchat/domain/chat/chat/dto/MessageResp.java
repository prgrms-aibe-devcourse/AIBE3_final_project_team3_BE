package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;

import java.time.LocalDateTime;

public record MessageResp(
        @NotNull
        @Schema(description = "메시지 고유 ID", example = "60c72b2f9b1d8e001f8e4bde")
        String id,

        @NotNull
        @Schema(description = "발신자 ID", example = "1")
        Long senderId,

        @NotNull
        @Schema(description = "발신자 닉네임", example = "JohnDoe")
        String sender,

        @NotNull
        @Schema(description = "메시지 내용", example = "안녕하세요!")
        String content,

        @NotNull
        @Schema(description = "메시지 발신 시간")
        LocalDateTime createdAt,

        @NotNull
        @Schema(description = "메시지 타입", example = "TALK")
        ChatMessage.MessageType messageType
) {
    public static MessageResp from(ChatMessage entity, String senderName) {
        return new MessageResp(
                entity.getId(),
                entity.getSenderId(),
                senderName,
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getMessageType()
        );
    }
}
