package triplestar.mixchat.domain.ai.rag.context.user;

import static triplestar.mixchat.domain.ai.rag.context.user.ContextChunkTextKey.LEARNING_NOTE_CORRECTED_CONTENT;
import static triplestar.mixchat.domain.ai.rag.context.user.ContextChunkTextKey.LEARNING_NOTE_ORIGINAL_CONTENT;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteRepository;

@Component
public class SqlContextRetriever implements ContextRetriever {

    private final LearningNoteRepository learningNoteRepository;
    private final int mintItems;
    private final int maxItems;

    public SqlContextRetriever(
            LearningNoteRepository learningNoteRepository,
            @Value("${ai.context-retriever.sql.min}")
            int minItems,
            @Value("${ai.context-retriever.sql.max}")
            int maxItems
    ) {
        this.learningNoteRepository = learningNoteRepository;
        this.mintItems = minItems;
        this.maxItems = maxItems;
    }

    @Override
    public List<UserContextChunk> retrieve(Long userId, String userMessage, int itemSize) {
        if (itemSize < mintItems || itemSize > maxItems) {
            throw new IllegalArgumentException(
                    "컨텍스트 청크 수는 %d에서 %d 사이여야 합니다.".formatted(mintItems, maxItems)
            );
        }

        List<LearningNote> notes = learningNoteRepository.findTopNByMemberId(userId, itemSize);

        // text 맵에 originalContent와 correctedContent를 모두 포함
        return notes.stream()
                .map(note -> new UserContextChunk(
                        // ContextChunkTextKey static import 사용
                        Map.of(LEARNING_NOTE_ORIGINAL_CONTENT.getKey(), note.getOriginalContent(),
                                LEARNING_NOTE_CORRECTED_CONTENT.getKey(), note.getCorrectedContent()),
                        ContextChunkType.LEARNING_NOTE,
                        note.getId(),
                        note.getCreatedAt()
                ))
                .toList();
    }
}
