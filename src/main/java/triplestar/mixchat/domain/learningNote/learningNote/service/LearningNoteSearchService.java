package triplestar.mixchat.domain.learningNote.learningNote.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.learningNote.learningNote.document.LearningNoteDocument;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteDocumentRepository;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteRepository;

@Service
@RequiredArgsConstructor
public class LearningNoteSearchService {
    private final LearningNoteDocumentRepository noteDocumentRepository;
    private final LearningNoteRepository learningNoteRepository;
    private final EmbeddingModel embeddingModel;

    public List<LearningNote> searchRelevantNotes(
            Long memberId,
            String query
    ) {

        float[] embedding = embeddingModel.embed(query);

        SearchPage<?> results = noteDocumentRepository.knnSearch(
                embedding,
                "embedding",
                10,
                100,
                PageRequest.of(0, 10),
                LearningNoteDocument.class
        );

        List<Long> ids = results.getContent().stream()
                .map(hit -> ((LearningNoteDocument) hit.getContent()).getNoteId())
                .toList();

        return learningNoteRepository.findAllById(ids);
    }
}
