package triplestar.mixchat.domain.chat.chat.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;

@Schema(description = "채팅 메시지 응답")
public record MessageResp(
        @Schema(description = "메시지 고유 ID", example = "60c72b2f9b1d8e001f8e4bde", requiredMode = REQUIRED)
        String id,

        @Schema(description = "발신자 ID", example = "1", requiredMode = REQUIRED)
        Long senderId,

        @Schema(description = "발신자 닉네임", example = "JohnDoe", requiredMode = REQUIRED)
        String sender,

        @Schema(description = "메시지 내용", example = "안녕하세요!", requiredMode = REQUIRED)
        String content,

        @Schema(description = "번역된 메시지 내용", example = "Hello!")
        String translatedContent,

        @Schema(description = "자동 번역 요청 여부", example = "true")
        Boolean isTranslateEnabled,

        @Schema(description = "메시지 발신 시간", requiredMode = REQUIRED)
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        LocalDateTime createdAt,

        @Schema(description = "메시지 타입", example = "TALK", requiredMode = REQUIRED)
        ChatMessage.MessageType messageType,

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
                entity.getTranslatedContent(),
                entity.isTranslateEnabled(),
                entity.getCreatedAt(),
                entity.getMessageType(),
                entity.getSequence(),
                null
        );
    }

    public static MessageResp withUnreadCount(ChatMessage entity, String senderName, int unreadCount) {
        return new MessageResp(
                entity.getId(),
                entity.getSenderId(),
                senderName,
                entity.getContent(),
                entity.getTranslatedContent(),
                entity.isTranslateEnabled(),
                entity.getCreatedAt(),
                entity.getMessageType(),
                entity.getSequence(),
                unreadCount
        );
    }
}
