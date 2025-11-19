package triplestar.mixchat.domain.miniGame.sentenceGame.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.global.jpa.entity.BaseEntityNoModified;

@Entity
@Table(name = "sentence_games")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SentenceGame extends BaseEntityNoModified {

    @Column(name = "original_content", nullable = false)
    private String originalContent;      // 수정 전 문장

    @Column(name = "corrected_content", nullable = false)
        private String correctedContent;     // 수정 후 문장

    private SentenceGame(
            String originalContent,
            String correctedContent
    ) {
        if (originalContent == null || originalContent.isBlank()) {
            throw new IllegalArgumentException("originalContent는 비어 있을 수 없습니다.");
        }
        if (correctedContent == null || correctedContent.isBlank()) {
            throw new IllegalArgumentException("correctedContent 비어 있을 수 없습니다.");
        }

        this.originalContent = originalContent;
        this.correctedContent = correctedContent;
    }

    public static SentenceGame createSentenceGame(
            String originalContent,
            String correctedContent
    ) {
        return new SentenceGame(
                originalContent,
                correctedContent
        );
    }
}
