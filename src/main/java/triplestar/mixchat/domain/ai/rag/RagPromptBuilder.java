package triplestar.mixchat.domain.ai.rag;

import static triplestar.mixchat.domain.ai.rag.context.user.ContextChunkTextKey.LEARNING_NOTE_CORRECTED_CONTENT;
import static triplestar.mixchat.domain.ai.rag.context.user.ContextChunkTextKey.LEARNING_NOTE_ORIGINAL_CONTENT;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import triplestar.mixchat.domain.ai.rag.context.user.ContextChunkType;
import triplestar.mixchat.domain.ai.rag.context.user.UserContextChunk;
import triplestar.mixchat.domain.ai.systemprompt.constant.PromptKey;
import triplestar.mixchat.domain.ai.systemprompt.entity.SystemPrompt;
import triplestar.mixchat.domain.ai.systemprompt.service.SystemPromptService;

@Component
@RequiredArgsConstructor
public class RagPromptBuilder {

    private final SystemPromptService systemPromptService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String buildPrompt(List<UserContextChunk> contextChunks, String persona) {
        // 1) DB에서 최신 버전 시스템 프롬프트 템플릿 가져오기
        SystemPrompt systemPrompt = systemPromptService.getLatestByKey(PromptKey.AI_TUTOR);
        String template = systemPrompt.getContent();

        // 2) 치환용 블록들 생성
        String learningNotesBlock = buildLearningNotesBlock(contextChunks);

        // 3) 템플릿 플레이스홀더 치환
        return template.replace("{{LEARNING_NOTES}}", learningNotesBlock)
                .replace("{{PERSONA}}", persona);
    }

    private String buildLearningNotesBlock(List<UserContextChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "(저장된 과거 학습노트가 없습니다. 일반적인 영어 코치처럼 동작해 주세요.)";
        }

        return chunks.stream()
                .filter(chunk -> chunk.type() == ContextChunkType.LEARNING_NOTE)
                .map(this::formatLearningNoteChunk)
                .collect(Collectors.joining("\n"));
    }

    private String formatLearningNoteChunk(UserContextChunk chunk) {
        Map<String, String> textMap = chunk.text();

        String original = textMap.get(LEARNING_NOTE_ORIGINAL_CONTENT.getKey());
        String corrected = textMap.get(LEARNING_NOTE_CORRECTED_CONTENT.getKey());

        String createdAtStr = DATE_FORMATTER.format(chunk.createdAt());

        StringBuilder sb = new StringBuilder();
        sb.append("[").append(createdAtStr).append("]");
        sb.append("Original: ").append(nullToPlaceholder(original));
        sb.append(" | Corrected: ").append(nullToPlaceholder(corrected));

        return sb.toString();
    }

    private String nullToPlaceholder(String value) {
        return value != null ? value : "(none)";
    }
}
