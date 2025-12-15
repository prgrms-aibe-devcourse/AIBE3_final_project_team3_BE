package triplestar.mixchat.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "관리자용 문장 등록 요청")
public record AdminSentenceGameCreateResp(
        @NotNull
        @Schema(description = "문장게임 ID", example = "1")
        Long sentenceGameId
){
    public static AdminSentenceGameCreateResp from(Long id) {
        return new AdminSentenceGameCreateResp(id);
    }
}