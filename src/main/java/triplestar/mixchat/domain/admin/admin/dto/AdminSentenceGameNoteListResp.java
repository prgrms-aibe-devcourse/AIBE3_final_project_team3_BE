package triplestar.mixchat.domain.admin.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;

public record AdminSentenceGameNoteListResp(

        @Schema(description = "학습노트 ID", example = "101")
        Long id,

        @Schema(description = "수정 전 문장", example = "I goed to the store yesterday.")
        String originalContent,

        @Schema(description = "수정 후 문장", example = "I went to the store yesterday.")
        String correctedContent
) {
    public static AdminSentenceGameNoteListResp from(LearningNote note) {
        return new AdminSentenceGameNoteListResp(
                note.getId(),
                note.getOriginalContent(),
                note.getCorrectedContent()
        );
    }
}