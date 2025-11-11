package triplestar.mixchat.domain.miniGame.sentenceGame.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

@Entity
@Table(name = "sentence_games")
public class SentenceGame extends BaseEntity {

    @Column(name = "original_content", nullable = false)
    private String originalContent;      // 수정 전 문장

    @Column(name = "corrected_content", nullable = false)
        private String correctedContent;     // 수정 후 문장

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false)
    private TranslationTagCode code;
}
