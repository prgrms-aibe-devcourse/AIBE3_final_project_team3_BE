package triplestar.mixchat.domain.ai.systemprompt.constant;

public enum PromptKey {
    AI_TUTOR_PROMPT("AI_TUTOR_PROMPT"),
    AI_ASSIST_PROMPT("AI_ASSIST_PROMPT"),
    ;

    private final String key;

    PromptKey(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public static PromptKey from(String key) {
        for (PromptKey value : values()) {
            if (value.key.equals(key)) return value;
        }
        throw new IllegalArgumentException("지원하지 않는 PromptKey: " + key);
    }
}