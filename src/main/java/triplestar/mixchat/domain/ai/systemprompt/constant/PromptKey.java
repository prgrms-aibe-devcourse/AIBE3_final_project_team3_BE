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
}