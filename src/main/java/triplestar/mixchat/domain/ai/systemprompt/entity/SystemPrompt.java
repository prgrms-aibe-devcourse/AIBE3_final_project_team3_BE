package triplestar.mixchat.domain.ai.systemprompt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.global.jpa.entity.BaseEntityNoModified;

@Entity
@Table(name = "system_prompts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SystemPrompt extends BaseEntityNoModified {

    // 고정된 타입이기 보다는 데이터베이스에 지속적으로 추가되는 형태이므로 Enum이 아닌 String으로 둠
    @Column(name = "prompt_key", nullable = false, unique = true, length = 100)
    private String promptKey;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer version;

    public SystemPrompt(String promptKey, String description, String content, Integer version) {
        validate(promptKey, description, content, version);
        this.promptKey = promptKey;
        this.description = description;
        this.content = content;
        this.version = version;
    }

    private void validate(String promptKey, String description, String content, Integer version) {
        if (promptKey == null || promptKey.isBlank()) {
            throw new IllegalArgumentException("Prompt key는 공백일 수 없습니다.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description는 공백일 수 없습니다.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content는 공백일 수 없습니다.");
        }
        if (version == null || version < 1) {
            throw new IllegalArgumentException("Version은 1 이상의 값이어야 합니다.");
        }
    }
}
