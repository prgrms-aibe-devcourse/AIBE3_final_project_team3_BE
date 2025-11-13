package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "그룹 채팅방 생성 요청")
public record CreateGroupChatReq(
        @NotBlank
        @Schema(description = "그룹 채팅방 이름", example = "프로젝트 그룹")
        String roomName,

        @NotEmpty
        @Schema(description = "채팅방에 초대할 회원 ID 목록", example = "[2, 3, 4]")
        List<Long> memberIds
) {
}
