package triplestar.mixchat.domain.learningNote.learningNote.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.learningNote.learningNote.document.LearningNoteDocument;
import triplestar.mixchat.domain.learningNote.learningNote.embedding.EmbeddingTextBuilder;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteDocumentRepository;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteRepository;
import triplestar.mixchat.global.cache.LearningNoteSearchCacheService;

@Service
public class LearningNoteSaveService {
    private final LearningNoteDocumentRepository noteDocumentRepository;
    private final LearningNoteRepository learningNoteRepository;
    private final EmbeddingModel embeddingModel;
    private final LearningNoteSearchCacheService learningNoteSearchCacheService;
    public LearningNoteSaveService(
            LearningNoteDocumentRepository noteDocumentRepository,
            LearningNoteRepository learningNoteRepository,
            @Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel,
            LearningNoteSearchCacheService learningNoteSearchCacheService
    ) {
        this.noteDocumentRepository = noteDocumentRepository;
        this.learningNoteRepository = learningNoteRepository;
        this.embeddingModel = embeddingModel;
        this.learningNoteSearchCacheService = learningNoteSearchCacheService;
    }

    public void saveByRecentNotes(Long roomId, Long memberId) {
        // 캐시 체크
        List<Long> cached = learningNoteSearchCacheService.get(roomId, memberId);
        if (cached != null) {
            return;
        }

        List<LearningNote> recentNotes = learningNoteRepository.findTopNByMemberId(memberId, 10);

        if (recentNotes.isEmpty()){
            return;
        }

        // 검색할 학습노트 id 저장
        List<Long> excludeIds = recentNotes.stream().map(LearningNote::getId).toList();

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

        // 본인 학습노트 제외
        excludeIds.forEach(scoreMap::remove);

        // 상위 10개 리턴
        List<Long> topIds = scoreMap.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .limit(10)
                .toList();

        learningNoteSearchCacheService.save(roomId, memberId, topIds);
    }

    public List<LearningNote> loadNotesFromCache(Long roomId, Long memberId) {
        List<Long> ids = learningNoteSearchCacheService.get(roomId, memberId);

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return learningNoteRepository.findAllById(ids);
    }
}
