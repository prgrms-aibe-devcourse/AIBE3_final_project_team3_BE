package triplestar.mixchat.domain.miniGame.sentenceGame.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record SentenceGameSubmitResp(
        @Schema(description = "정답 여부", example = "true")
        boolean correct,

        @NotBlank
        @Schema(description = "정답 문장", example = "I went to school.")
        String correctedContent
){
    public static SentenceGameSubmitResp correct(String corrected) {
        return new SentenceGameSubmitResp(true, corrected);
    }

    public static SentenceGameSubmitResp wrong(String corrected) {
        return new SentenceGameSubmitResp(false, corrected);
    }
}