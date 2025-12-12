package triplestar.mixchat.domain.ai.rag.promptbuilder;

import static triplestar.mixchat.domain.ai.rag.context.user.ContextChunkTextKey.LEARNING_NOTE_CORRECTED_CONTENT;
import static triplestar.mixchat.domain.ai.rag.context.user.ContextChunkTextKey.LEARNING_NOTE_ORIGINAL_CONTENT;
import static triplestar.mixchat.domain.chat.chat.constant.AiChatRoomType.TUTOR_PERSONAL;
import static triplestar.mixchat.domain.chat.chat.constant.AiChatRoomType.TUTOR_SIMILAR;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Component;
import triplestar.mixchat.domain.ai.rag.context.user.UserContextChunk;
import triplestar.mixchat.domain.ai.systemprompt.constant.PromptKey;
import triplestar.mixchat.domain.ai.systemprompt.entity.SystemPrompt;
import triplestar.mixchat.domain.ai.systemprompt.service.SystemPromptService;
import triplestar.mixchat.domain.chat.chat.constant.AiChatRoomType;

@Component
@RequiredArgsConstructor
public class RagPromptBuilder {

    private final SystemPromptService systemPromptService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public List<Message> convertChunksToMessages(List<UserContextChunk> chunks) {
        return chunks.stream()
                .map(chunk -> (Message) new SystemMessage(
                        "학습노트 날짜: " + DATE_FORMATTER.format(chunk.createdAt()) + "\n" +
                                "원문: " + chunk.text().get(LEARNING_NOTE_ORIGINAL_CONTENT.getKey()) + "\n" +
                                "수정문: " + chunk.text().get(LEARNING_NOTE_CORRECTED_CONTENT.getKey())
                ))
                .collect(Collectors.toList());
    }

    public String buildPrompt(String persona, AiChatRoomType roomType) {
        // 1) DB에서 최신 버전 시스템 프롬프트 템플릿 가져오기
        SystemPrompt systemPrompt = switch (roomType) {
            case TUTOR_PERSONAL -> systemPromptService.getLatestByKey(PromptKey.AI_PERSONAL_TUTOR);
            case TUTOR_SIMILAR  -> systemPromptService.getLatestByKey(PromptKey.AI_SIMILAR_TUTOR);
            default -> throw new IllegalArgumentException("지원하지 않는 채팅방 타입입니다: " + roomType);
        };

        String template = systemPrompt.getContent();

        return template.replace("{{PERSONA}}", persona);
    }
}
