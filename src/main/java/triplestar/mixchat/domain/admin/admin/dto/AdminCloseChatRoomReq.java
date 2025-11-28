package triplestar.mixchat.domain.admin.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "관리자의 방 폐쇄 사유")
public record AdminCloseChatRoomReq(
        @Schema(description = "방 폐쇄 사유", example = "불건전한 대화")
        @NotBlank
        String reason
) {}