package triplestar.mixchat.domain.learningNote.learningNote.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import triplestar.mixchat.domain.learningNote.learningNote.document.LearningNoteDocument;

public interface LearningNoteDocumentRepository extends ElasticsearchRepository<LearningNoteDocument, Long>,
        LearningNoteKnnSearchRepository {
}