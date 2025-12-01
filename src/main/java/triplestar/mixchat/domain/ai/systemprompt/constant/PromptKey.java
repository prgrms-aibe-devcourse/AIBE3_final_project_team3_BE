package triplestar.mixchat.domain.ai.systemprompt.constant;

public enum PromptKey {
    AI_TUTOR("AI_TUTOR"),
    AI_ASSIST("AI_ASSIST"),
    ;

    private final String key;

    PromptKey(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}