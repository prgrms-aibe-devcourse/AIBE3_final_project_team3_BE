package triplestar.mixchat.domain.miniGame.sentenceGame.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import triplestar.mixchat.domain.miniGame.sentenceGame.entity.SentenceGame;

public interface  SentenceGameRepository extends JpaRepository<SentenceGame, Long> {
    boolean existsByOriginalContentAndCorrectedContent(String originalContent, String correctedContent);
}
