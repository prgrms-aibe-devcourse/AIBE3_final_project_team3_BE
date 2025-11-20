package triplestar.mixchat.domain.miniGame.sentenceGame.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자의 입력에 대한 정답 여부와 정답 문장")
public record SentenceGameSubmitResp(
        @Schema(description = "정답 여부", example = "true", requiredMode = REQUIRED)
        boolean correct,

        @Schema(description = "정답 문장", example = "I went to school.", requiredMode = REQUIRED)
        String correctedContent
){
    public static SentenceGameSubmitResp correct(String corrected) {
        return new SentenceGameSubmitResp(true, corrected);
    }

    public static SentenceGameSubmitResp wrong(String corrected) {
        return new SentenceGameSubmitResp(false, corrected);
    }
}