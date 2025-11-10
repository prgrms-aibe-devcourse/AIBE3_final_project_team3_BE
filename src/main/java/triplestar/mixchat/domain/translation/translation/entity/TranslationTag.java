package triplestar.mixchat.domain.translation.translation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;
import triplestar.mixchat.global.jpa.entity.BaseEntity;

@NoArgsConstructor
@Entity
@Table(name = "translation_tags")
public class TranslationTag extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TranslationTagCode code;


    public TranslationTag(TranslationTagCode code) {
        this.code = code;
    }
}