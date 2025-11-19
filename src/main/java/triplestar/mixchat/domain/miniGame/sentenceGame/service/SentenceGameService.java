package triplestar.mixchat.domain.miniGame.sentenceGame.service;

import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.miniGame.sentenceGame.dto.SentenceGameStartResp;
import triplestar.mixchat.domain.miniGame.sentenceGame.dto.SentenceGameSubmitReq;
import triplestar.mixchat.domain.miniGame.sentenceGame.dto.SentenceGameSubmitResp;
import triplestar.mixchat.domain.miniGame.sentenceGame.entity.SentenceGame;
import triplestar.mixchat.domain.miniGame.sentenceGame.repository.SentenceGameRepository;

@Service
@RequiredArgsConstructor
public class SentenceGameService {
    private final SentenceGameRepository sentenceGameRepository;

    @Transactional
    public long getTotalCount() {
        return sentenceGameRepository.count();
    }

    @Transactional
    public SentenceGameStartResp startGame(int count) {
        long total = sentenceGameRepository.count();

        if (count <= 0) {
            throw new IllegalArgumentException("문제 수는 1 이상이어야 합니다.");
        }

        if (count > total) {
            throw new IllegalArgumentException("요청한 문제 수(" + count + ")가 등록된 총 문제 수(" + total + ")보다 많습니다.");
        }

        List<SentenceGame> all = sentenceGameRepository.findAll();
        Collections.shuffle(all);

        List<SentenceGameStartResp.QuestionItem> selected = all.stream()
                .limit(count)
                .map(SentenceGameStartResp.QuestionItem::from)
                .toList();

        return new SentenceGameStartResp(selected);
    }

    @Transactional
    public SentenceGameSubmitResp submitAnswer(SentenceGameSubmitReq req) {

        SentenceGame game = sentenceGameRepository.findById(req.sentenceGameId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문장입니다."));

        boolean correct = game.getCorrectedContent()
                .equalsIgnoreCase(req.userAnswer().trim());

        return new SentenceGameSubmitResp(
                correct,
                game.getCorrectedContent()
        );
    }
}