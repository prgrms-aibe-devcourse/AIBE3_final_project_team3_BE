package triplestar.mixchat.domain.learningNote.learningNote.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학습노트 정보 응답")
public record LearningNoteResp(
        @Schema(description = "학습노트 ID", example = "5", requiredMode = REQUIRED)
        Long id,

        @Schema(description = "원본 문장", example = "I goes to school yesterday.", requiredMode = REQUIRED)
        String originalContent,

        @Schema(description = "AI가 수정한 최종 문장", example = "I went to school yesterday.", requiredMode = REQUIRED)
        String correctedContent
) {}