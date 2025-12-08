package triplestar.mixchat.domain.miniGame.sentenceGame.service;

import jakarta.persistence.EntityNotFoundException;
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
        } else if(count > 20) {
            throw new IllegalArgumentException("요청한 문제 수(" + count + ")가 너무 많습니다.");
        }

        List<SentenceGame> all = sentenceGameRepository.findAll();
        Collections.shuffle(all);

        return SentenceGameStartResp.from(
                all.stream()
                        .limit(count)
                        .toList()
        );
    }

    @Transactional
    public SentenceGameSubmitResp submitAnswer(SentenceGameSubmitReq req) {

        SentenceGame game = sentenceGameRepository.findById(req.sentenceGameId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 문장입니다."));

        String answer = normalize(game.getCorrectedContent());
        String userAnswer = normalize(req.userAnswer());

        boolean correct = answer.equals(userAnswer);

        return new SentenceGameSubmitResp(
                correct,
                game.getCorrectedContent(),
                game.getFeedbacks()
        );
    }

    private String normalize(String text) {
        if (text == null) return "";

        return text
                .toLowerCase()                     // 대소문자 무시 비교를 위해
                .replaceAll("\\p{Punct}", "")    // 모든 문장부호 제거 (.,!?;:"' 등)
                .replaceAll("\\s+", " ")           // 여러 공백 → 1칸 공백
                .trim();                            // 앞뒤 공백 제거
    }
}