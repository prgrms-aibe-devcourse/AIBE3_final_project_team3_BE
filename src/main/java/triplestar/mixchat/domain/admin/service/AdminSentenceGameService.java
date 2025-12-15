package triplestar.mixchat.domain.admin.service;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.admin.dto.AdminSentenceGameCreateReq;
import triplestar.mixchat.domain.admin.dto.AdminSentenceGameNoteResp;
import triplestar.mixchat.domain.admin.dto.AdminSentenceGameResp;
import triplestar.mixchat.domain.admin.exception.DuplicateSentenceGameException;
import triplestar.mixchat.domain.learningNote.entity.LearningNote;
import triplestar.mixchat.domain.learningNote.repository.LearningNoteRepository;
import triplestar.mixchat.domain.miniGame.sentenceGame.entity.FeedbackSnapshot;
import triplestar.mixchat.domain.miniGame.sentenceGame.entity.SentenceGame;
import triplestar.mixchat.domain.miniGame.sentenceGame.repository.SentenceGameRepository;

@Service
@RequiredArgsConstructor
public class AdminSentenceGameService {
    private final SentenceGameRepository sentenceGameRepository;
    private final LearningNoteRepository learningNoteRepository;

    @Transactional
    public Long createSentenceGame(AdminSentenceGameCreateReq request) {

        LearningNote note = learningNoteRepository.findById(request.learningNoteId())
                .orElseThrow(() -> new IllegalArgumentException("학습노트를 찾을 수 없습니다. id:" + request.learningNoteId()));

        boolean duplicated = sentenceGameRepository.existsByOriginalContentAndCorrectedContent(
                note.getOriginalContent(),
                note.getCorrectedContent()
        );

        if (duplicated) {
            throw new DuplicateSentenceGameException();
        }

        List<FeedbackSnapshot> snapshots = note.getFeedbacks().stream()
                .map(f -> new FeedbackSnapshot(
                        f.getTag().name(),
                        f.getProblem(),
                        f.getCorrection(),
                        f.getExtra()
                ))
                .toList();

        SentenceGame game = SentenceGame.createSentenceGame(
                note.getOriginalContent(),
                note.getCorrectedContent(),
                snapshots
        );

        return sentenceGameRepository.save(game).getId();
    }

    @Transactional(readOnly = true)
    public Page<AdminSentenceGameNoteResp> getSentenceGameNoteList(Pageable pageable) {

        Page<LearningNote> notes = learningNoteRepository.findAll(pageable);

        return notes.map(AdminSentenceGameNoteResp::from);
    }

    @Transactional(readOnly = true)
    public Page<AdminSentenceGameResp> getSentenceGameList(Pageable pageable) {

        Page<SentenceGame> games = sentenceGameRepository.findAll(pageable);

        return games.map(AdminSentenceGameResp::from);
    }

    @Transactional
    public void deleteSentenceGame(Long sentenceGameId) {
        SentenceGame game = sentenceGameRepository.findById(sentenceGameId)
                .orElseThrow(() -> new IllegalArgumentException("해당 문장을 찾을 수 없습니다. id=" + sentenceGameId));

        sentenceGameRepository.delete(game);
    }
}