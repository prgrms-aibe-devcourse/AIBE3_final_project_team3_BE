package triplestar.mixchat.domain.learningNote.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchPage;

public interface LearningNoteKnnSearchRepository {
    SearchPage<?> knnSearch(
            float[] queryVector,
            String field,
            int k,
            int numCandidates,
            Pageable pageable,
            Class<?> clazz
    );
}