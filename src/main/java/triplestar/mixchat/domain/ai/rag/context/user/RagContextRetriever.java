package triplestar.mixchat.domain.ai.rag.context.user;

import static triplestar.mixchat.domain.ai.rag.context.user.ContextChunkTextKey.LEARNING_NOTE_CORRECTED_CONTENT;
import static triplestar.mixchat.domain.ai.rag.context.user.ContextChunkTextKey.LEARNING_NOTE_ORIGINAL_CONTENT;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.learningNote.service.LearningNoteRagService;

@Component
@RequiredArgsConstructor
public class RagContextRetriever {
    private final LearningNoteRagService learningNoteRagService;

    public List<UserContextChunk> retrieve(Long roomId, Long userId) {
        List<LearningNote> notes = learningNoteRagService.loadNotesFromCache(roomId, userId);

        if(notes == null || notes.isEmpty()) {
            return List.of();
        }

        return notes.stream()
                .map(note -> new UserContextChunk(
                        Map.of(LEARNING_NOTE_ORIGINAL_CONTENT.getKey(), note.getOriginalContent(),
                                LEARNING_NOTE_CORRECTED_CONTENT.getKey(), note.getCorrectedContent()),
                        ContextChunkType.LEARNING_NOTE,
                        note.getId(),
                        note.getCreatedAt()
                ))
                .toList();
    }
}
