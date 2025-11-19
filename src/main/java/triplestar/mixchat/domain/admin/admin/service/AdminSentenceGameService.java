package triplestar.mixchat.domain.admin.admin.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.admin.admin.dto.AdminSentenceGameCreateReq;
import triplestar.mixchat.domain.admin.admin.dto.AdminSentenceGameNoteResp;
import triplestar.mixchat.domain.learningNote.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.learningNote.repository.LearningNoteRepository;
import triplestar.mixchat.domain.miniGame.sentenceGame.entity.SentenceGame;
import triplestar.mixchat.domain.miniGame.sentenceGame.repository.SentenceGameRepository;

@Service
@RequiredArgsConstructor
public class AdminSentenceGameService {
    private final SentenceGameRepository sentenceGameRepository;
    private final LearningNoteRepository learningNoteRepository;

    @Transactional
    public Long createSentenceGame(AdminSentenceGameCreateReq request) {

        boolean duplicated = sentenceGameRepository.existsByOriginalContentAndCorrectedContent(
                request.originalContent(),
                request.correctedContent()
        );

        if (duplicated) {
            throw new IllegalArgumentException("이미 등록된 문장입니다.");
        }

        SentenceGame game = SentenceGame.createSentenceGame(
                request.originalContent(),
                request.correctedContent()
        );
        return sentenceGameRepository.save(game).getId();
    }

    @Transactional(readOnly = true)
    public Page<AdminSentenceGameNoteResp> getSentenceGameNoteList(Pageable pageable) {

        Page<LearningNote> notes = learningNoteRepository.findAll(pageable);

        return notes.map(AdminSentenceGameNoteResp::from);
    }
}