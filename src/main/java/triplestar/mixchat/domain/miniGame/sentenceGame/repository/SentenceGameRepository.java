package triplestar.mixchat.domain.miniGame.sentenceGame.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import triplestar.mixchat.domain.miniGame.sentenceGame.entity.SentenceGame;

public interface  SentenceGameRepository extends JpaRepository<SentenceGame, Long> {
    boolean existsByOriginalContentAndCorrectedContent(String originalContent, String correctedContent);
}
