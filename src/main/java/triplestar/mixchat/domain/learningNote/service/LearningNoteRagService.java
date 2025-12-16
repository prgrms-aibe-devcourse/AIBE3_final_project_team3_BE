package triplestar.mixchat.domain.learningNote.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.learningNote.document.LearningNoteDocument;
import triplestar.mixchat.domain.learningNote.embedding.EmbeddingTextBuilder;
import triplestar.mixchat.domain.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.repository.LearningNoteDocumentRepository;
import triplestar.mixchat.domain.learningNote.repository.LearningNoteRepository;
import triplestar.mixchat.global.cache.LearningNoteCacheRepository;

@Service
public class LearningNoteRagService {

    private final LearningNoteDocumentRepository noteDocumentRepository;
    private final LearningNoteRepository learningNoteRepository;
    private final EmbeddingModel embeddingModel;
    private final LearningNoteCacheRepository learningNoteCacheRepository;

    public LearningNoteRagService(
            LearningNoteDocumentRepository noteDocumentRepository,
            LearningNoteRepository learningNoteRepository,
            @Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel,
            LearningNoteCacheRepository learningNoteCacheRepository
    ) {
        this.noteDocumentRepository = noteDocumentRepository;
        this.learningNoteRepository = learningNoteRepository;
        this.embeddingModel = embeddingModel;
        this.learningNoteCacheRepository = learningNoteCacheRepository;
    }

    public void saveByRecentNotes(Long roomId, Long memberId) {
        // 캐시 체크
        List<Long> cached = learningNoteCacheRepository.get(roomId, memberId);
        if (cached != null) {
            return;
        }

        List<LearningNote> recentNotes = learningNoteRepository.findTopNByMemberId(memberId, PageRequest.of(0, 10));

        if (recentNotes.isEmpty()){
            return;
        }

        Map<Long, Double> scoreMap = new HashMap<>();

        for (LearningNote note : recentNotes) {

            String text = EmbeddingTextBuilder.build(note);
            float[] embedding = embeddingModel.embed(text);

            SearchPage<?> results = noteDocumentRepository.knnSearch(
                    embedding,
                    "embedding",
                    10,
                    100,
                    PageRequest.of(0, 10),
                    LearningNoteDocument.class
            );

            // 검색 점수 합산
            results.getContent().forEach(hit -> {
                Long id = ((LearningNoteDocument) hit.getContent()).getNoteId();
                double score = hit.getScore();

                scoreMap.merge(id, score, Double::sum);
            });
        }

        // 결과에서 제외할 학습노트 제거
        List<Long> excludeIds = recentNotes.stream().map(LearningNote::getId).toList();

        excludeIds.forEach(scoreMap::remove);

        // 상위 10개 리턴
        List<Long> topIds = scoreMap.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .limit(10)
                .toList();

        learningNoteCacheRepository.save(roomId, memberId, topIds);
    }

    public List<LearningNote> loadNotesFromCache(Long roomId, Long memberId) {
        List<Long> ids = learningNoteCacheRepository.get(roomId, memberId);

        if (ids == null || ids.isEmpty()) {
            saveByRecentNotes(roomId, memberId);
            ids = learningNoteCacheRepository.get(roomId, memberId);
        }

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return learningNoteRepository.findAllById(ids);
    }
}
