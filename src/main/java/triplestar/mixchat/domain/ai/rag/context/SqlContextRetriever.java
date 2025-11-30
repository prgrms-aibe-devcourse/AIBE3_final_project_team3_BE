package triplestar.mixchat.domain.ai.rag.context;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteRepository;

@RequiredArgsConstructor
@Component
@Primary // 기본 전략
public class SqlContextRetriever implements ContextRetriever {

    private final LearningNoteRepository learningNoteRepository;

    @Override
    public List<UserContextChunk> retrieve(Long userId, String userMessage, int maxItems) {
        List<LearningNote> notes =
                learningNoteRepository.findTopNByUserIdOrderByCreatedAtDesc(userId, maxItems);

        return notes.stream()
                .map(note -> new UserContextChunk(
                        note.getContent(),
                        "LEARNING_NOTE",
                        note.getId(),
                        null
                ))
                .toList();
    }
}
