package triplestar.mixchat.domain.learningNote.embedding.operation;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.global.response.CustomResponse;

@Profile({"local", "dev"})
@RestController
@RequestMapping("/api/v1/admin/dev/embedding")
@RequiredArgsConstructor
public class EmbeddingOperationController {

    private final EmbeddingOperationService embeddingOperationService;

    @PostMapping()
    public CustomResponse<String> regenerateAll() {
        int count = embeddingOperationService.regenerateAllLearningNoteEmbeddings();
        String msg = "학습노트 임베딩 재생성 완료: " + count + "개 문서 처리됨";
        return CustomResponse.ok(msg, msg);
    }
}