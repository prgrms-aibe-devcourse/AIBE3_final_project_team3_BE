package triplestar.mixchat.domain.notification.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import triplestar.mixchat.domain.notification.constant.NotificationType;

@Schema(description = "알림 목록 조회 응답 DTO")
public record NotificationResp(
        @Schema(description = "알림의 고유 ID", example = "55", requiredMode = REQUIRED)
        Long id,

        @Schema(description = "알림을 수신한 사용자 ID", example = "1", requiredMode = REQUIRED)
        Long receiverId,

        @Schema(description = "알림을 발생시킨 사용자의 ID", example = "2", requiredMode = REQUIRED)
        Long senderId,

        @Schema(description = "알림을 발생시킨 사용자의 닉네임", example = "mixChatUser", requiredMode = REQUIRED)
        String senderNickname,

        @Schema(description = "알림의 종류", example = "FRIEND_REQUEST", requiredMode = REQUIRED)
        NotificationType type,

        @Schema(description = "알림 읽음 여부", example = "false", requiredMode = REQUIRED)
        boolean isRead,

        @Schema(description = "알림 생성 시간", example = "2025-11-17T10:30:00", requiredMode = REQUIRED)
        LocalDateTime createdAt,

        @Schema(description = "알림 내용 (추가 텍스트)", example = "시스템 업데이트가 예정되어 있습니다.", requiredMode = REQUIRED)
        String extraContent
) {
}