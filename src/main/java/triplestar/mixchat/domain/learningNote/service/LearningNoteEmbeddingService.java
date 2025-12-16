package triplestar.mixchat.domain.learningNote.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.learningNote.document.LearningNoteDocument;
import triplestar.mixchat.domain.learningNote.embedding.EmbeddingTextBuilder;
import triplestar.mixchat.domain.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.repository.LearningNoteDocumentRepository;

@Service
public class LearningNoteEmbeddingService {
    private final EmbeddingModel embeddingModel;
    private final LearningNoteDocumentRepository noteDocumentRepository;

    public LearningNoteEmbeddingService(
            @Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel,
            LearningNoteDocumentRepository noteDocumentRepository
    ) {
        this.embeddingModel = embeddingModel;
        this.noteDocumentRepository = noteDocumentRepository;
    }


    @Transactional
    public void index(LearningNote note) {

        // 1) 텍스트 빌드
        String text = EmbeddingTextBuilder.build(note);

        // 2) 임베딩 생성
        float[] embedding = embeddingModel.embed(text);

        // 3) 문서 저장
        LearningNoteDocument document = new LearningNoteDocument(embedding, note.getId());
        noteDocumentRepository.save(document);
    }
}