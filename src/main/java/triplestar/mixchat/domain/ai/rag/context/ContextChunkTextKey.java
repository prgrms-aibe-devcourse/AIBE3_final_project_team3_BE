package triplestar.mixchat.domain.ai.rag.context;

import lombok.Getter;

@Getter
public enum ContextChunkTextKey {
    LEARNING_NOTE_ORIGINAL_CONTENT("originalContent"),
    LEARNING_NOTE_CORRECTED_CONTENT("correctedContent"),
    ;

    private final String key;

    ContextChunkTextKey(String key) {
        this.key = key;
    }
}
