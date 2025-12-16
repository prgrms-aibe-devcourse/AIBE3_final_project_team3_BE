package triplestar.mixchat.domain.learningNote.repository;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LearningNoteDocumentRepositoryImpl implements LearningNoteKnnSearchRepository {
    private final ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public SearchPage<?> knnSearch(
            float[] queryVector,
            String field,
            int k,
            int numCandidates,
            Pageable pageable,
            Class<?> clazz
    ) {

        NativeQuery query = NativeQuery.builder()
                .withKnnSearches(knnSearchBuilder -> knnSearchBuilder
                        .field(field)
                        .queryVector(toList(queryVector))
                        .k(k)
                        .numCandidates(numCandidates)
                )
                .withPageable(pageable)
                .build();

        SearchHits<?> searchHits = elasticsearchTemplate.search(query, clazz);
        return SearchHitSupport.searchPageFor(searchHits, pageable);
    }

    private List<Float> toList(float[] arr) {
        Float[] boxed = new Float[arr.length];
        for (int i = 0; i < arr.length; i++) boxed[i] = arr[i];
        return Arrays.asList(boxed);
    }
}