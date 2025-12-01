package triplestar.mixchat.domain.ai.rag.service;

import static triplestar.mixchat.domain.ai.rag.context.user.ContextChunkTextKey.LEARNING_NOTE_CORRECTED_CONTENT;
import static triplestar.mixchat.domain.ai.rag.context.user.ContextChunkTextKey.LEARNING_NOTE_ORIGINAL_CONTENT;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import triplestar.mixchat.domain.ai.rag.context.chathistory.ChatTurn;
import triplestar.mixchat.domain.ai.rag.context.user.ContextChunkType;
import triplestar.mixchat.domain.ai.rag.context.user.UserContextChunk;

// TODO : 프롬프트 양식 systemPrompt sql 초기화 파일로 분리
@Component
public class RagPromptBuilder {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String buildPrompt(String userMessage, List<UserContextChunk> contextChunks, List<ChatTurn> chatHistory) {
        StringBuilder sb = new StringBuilder();

        // 1) 역할 정의
        sb.append("""
                너는 영어 회화 연습 앱 "Mixchat"에서 활동하는 AI 영어 코치야.
                
                [역할]
                
                1) 사용자의 영어 문장을 자연스럽고 정확하게 교정한다.
                2) 중요한 문법, 표현, 뉘앙스를 너무 길지 않게 간단히 설명한다.
                3) 사용자의 과거 학습노트를 참고해서, 이 사용자가 자주 틀렸던 패턴을 짚어주고 복습을 도와준다.
                
                [응답 언어 / 톤]
                
                - 기본적으로는 영어로 답변한다.
                - 필요할 때만 짧은 한국어 설명을 괄호 안에 덧붙인다. (예: ~~~ (과거완료 시제))
                - 톤은 친절하고 코치처럼, 하지만 과하게 장황하지 않게 답변한다.
                
                """);

        // 2) 과거 학습 노트 섹션
        if (contextChunks != null && !contextChunks.isEmpty()) {
            sb.append("## 사용자의 과거 학습노트 (Original → Corrected)\n");
            sb.append("아래 문장들을 참고해서, 사용자가 어떤 문법/표현에 약한지 추측해라.\n\n");

            String notesBlock = contextChunks.stream()
                    .filter(chunk -> chunk.type() == ContextChunkType.LEARNING_NOTE)
                    .map(this::formatLearningNoteChunk)
                    .collect(Collectors.joining("\n"));

            sb.append(notesBlock).append("\n\n");
        } else {
            sb.append("## 사용자의 과거 학습노트\n");
            sb.append("과거 학습노트가 없다. 일반적인 영어 회화 코치처럼 동작해라.\n\n");
        }

        // 3) 지금까지의 대화 로그 제공
        if (chatHistory != null && !chatHistory.isEmpty()) {
            sb.append("## 지금까지의 대화 (가장 오래된 순)\n");
            sb.append("아래 대화 흐름을 참고해서, 맥락에 맞게 이어서 답변해라.\n\n");

            String historyBlock = chatHistory.stream()
                    .map(this::formatChatTurn)
                    .collect(Collectors.joining("\n"));

            sb.append(historyBlock).append("\n\n");
        } else {
            sb.append("## 지금까지의 대화\n");
            sb.append("이전 대화 맥락은 없다. 일반적인 첫 대화처럼 응답해라.\n\n");
        }

        // 4) 현재 유저 발화 + 응답 규칙
        sb.append("## 현재 사용자의 입력 문장\n");
        sb.append(userMessage).append("\n\n");

        sb.append("""
                ## 답변 가이드
                
                1) 먼저, 교정된 문장을 한 줄로 제시한다.
                   - 가능하면 "Corrected: ..." 형태로 시작해도 좋다.
                
                2) 그 다음, 중요한 문법/표현 포인트를 짧게 정리해라.
                   - 예:
                     - Grammar: ~~~
                     - Expression: ~~~
                
                3) 위의 과거 학습노트와 관련 있는 실수라면,
                   - 사용자가 예전에 비슷한 실수를 했다는 것을 언급하고
                   - 같은 패턴을 연습할 수 있는 예문 1~2개를 추가로 제시한다.
                
                4) 마지막으로, 사용자가 스스로 연습해볼 수 있는 간단한 미션을 한 줄로 제안해라.
                   - 예: "Try to make 2 more sentences using 'would rather ~' on your own."
                """);

        return sb.toString();
    }

    /**
     * LEARNING_NOTE 타입 ContextChunk를 프롬프트용 문자열로 렌더링.
     */
    private String formatLearningNoteChunk(UserContextChunk chunk) {
        Map<String, String> textMap = chunk.text();

        String original = textMap.get(LEARNING_NOTE_ORIGINAL_CONTENT.getKey());
        String corrected = textMap.get(LEARNING_NOTE_CORRECTED_CONTENT.getKey());

        String createdAtStr = DATE_FORMATTER.format(chunk.createdAt());

        StringBuilder sb = new StringBuilder();
        sb.append("- [").append(createdAtStr).append("] ");
        sb.append("Original: ").append(nullToPlaceholder(original));
        sb.append(" | Corrected: ").append(nullToPlaceholder(corrected));

        return sb.toString();
    }

    private String nullToPlaceholder(String value) {
        return value != null ? value : "(none)";
    }

    private String formatChatTurn(ChatTurn turn) {
        String roleLabel = switch (turn.sender()) {
            case USER -> "User";
            case AI -> "AiTutor";
        };

        String timeStr = DATE_FORMATTER.format(turn.createdAt());

        return roleLabel + " : " + turn.content() + "  [" + timeStr + "]";
    }
}
