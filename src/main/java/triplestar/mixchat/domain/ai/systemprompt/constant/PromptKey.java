package triplestar.mixchat.domain.ai.systemprompt.constant;

// AI 시스템 프롬프트 키
// DB에 저장된 프롬프트를 식별하는 데 사용
public enum PromptKey {
    AI_TUTOR("AI_TUTOR"),
    AI_ASSIST("AI_ASSIST"),
    AI_ROLE_PLAY("AI_ROLE_PLAY"),
    ;

    private final String key;

    PromptKey(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}