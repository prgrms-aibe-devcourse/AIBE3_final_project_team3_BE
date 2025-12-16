package triplestar.mixchat.domain.learningNote.embedding.operation;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.repository.LearningNoteRepository;
import triplestar.mixchat.domain.learningNote.service.LearningNoteEmbeddingService;

@Service
@RequiredArgsConstructor
public class EmbeddingOperationService {

    private final LearningNoteRepository learningNoteRepository;
    private final LearningNoteEmbeddingService embeddingService;

    @Transactional
    public int regenerateAllLearningNoteEmbeddings() {
        List<LearningNote> notes = learningNoteRepository.findAllWithFeedbacks();

        int count = 0;
        for (LearningNote note : notes) {
            embeddingService.index(note);
            count++;
        }
        return count;
    }
}