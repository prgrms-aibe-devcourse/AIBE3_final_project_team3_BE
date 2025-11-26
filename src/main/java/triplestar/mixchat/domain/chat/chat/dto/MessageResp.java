package triplestar.mixchat.domain.chat.chat.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;

@Schema(description = "채팅 메시지 응답")
public record MessageResp(
        @NotBlank
        @Schema(description = "메시지 고유 ID", example = "60c72b2f9b1d8e001f8e4bde", requiredMode = REQUIRED)
        String id,

        @NotNull
        @Schema(description = "발신자 ID", example = "1", requiredMode = REQUIRED)
        Long senderId,

        @NotBlank
        @Schema(description = "발신자 닉네임", example = "JohnDoe", requiredMode = REQUIRED)
        String sender,

        @NotBlank
        @Schema(description = "메시지 내용", example = "안녕하세요!", requiredMode = REQUIRED)
        String content,

        @NotNull
        @Schema(description = "메시지 발신 시간", requiredMode = REQUIRED)
        LocalDateTime createdAt,

        @NotNull
        @Schema(description = "메시지 타입", example = "TALK", requiredMode = REQUIRED)
        ChatMessage.MessageType messageType,

        @NotNull
        @Schema(description = "메시지 순서 번호", example = "5", requiredMode = REQUIRED)
        Long sequence,

        @Schema(description = "읽지 않은 사람 수", example = "3")
        Integer unreadCount
) {
    public static MessageResp from(ChatMessage entity, String senderName) {
        return new MessageResp(
                entity.getId(),
                entity.getSenderId(),
                senderName,
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getMessageType(),
                entity.getSequence(),
                null
        );
    }

    public static MessageResp from(ChatMessage entity, String senderName, int unreadCount) {
        return new MessageResp(
                entity.getId(),
                entity.getSenderId(),
                senderName,
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getMessageType(),
                entity.getSequence(),
                unreadCount
        );
    }
}
