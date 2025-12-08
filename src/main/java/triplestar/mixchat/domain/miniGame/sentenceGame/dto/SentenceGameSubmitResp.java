package triplestar.mixchat.domain.miniGame.sentenceGame.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import triplestar.mixchat.domain.miniGame.sentenceGame.entity.FeedbackSnapshot;

@Schema(description = "사용자의 입력에 대한 정답 여부와 정답 문장")
public record SentenceGameSubmitResp(
        @Schema(description = "정답 여부", example = "true", requiredMode = REQUIRED)
        boolean correct,

        @Schema(description = "정답 문장", example = "I went to school.", requiredMode = REQUIRED)
        String correctedContent,

        @Schema(description = "피드백 리스트", example = "{tag: Grammar, problem: go, correction: went, extra: 과거형을 사용해야 합니다.}", requiredMode = REQUIRED)
        List<FeedbackSnapshot> feedbacks
){
    public static SentenceGameSubmitResp correct(String corrected, List<FeedbackSnapshot> feedbacks) {
        return new SentenceGameSubmitResp(true, corrected, feedbacks);
    }

    public static SentenceGameSubmitResp wrong(String corrected, List<FeedbackSnapshot> feedbacks) {
        return new SentenceGameSubmitResp(false, corrected, feedbacks);
    }
}