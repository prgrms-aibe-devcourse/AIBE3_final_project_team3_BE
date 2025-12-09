package triplestar.mixchat.domain.chat.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "부하테스트 데이터 정리 요청")
public record LoadTestCleanupReq(
        @Schema(description = "테스트 계정 ID 목록", example = "[101, 102, 103]", required = true)
        @NotEmpty(message = "테스트 계정 ID 목록은 비어있을 수 없습니다")
        List<Long> testAccountIds,

        @Schema(description = "이 시간 이후 생성된 데이터만 삭제 (선택사항)", example = "2025-12-09T10:00:00")
        LocalDateTime createdAfter,

        @Schema(description = "실제 삭제 없이 삭제될 데이터 미리보기 (선택사항, 기본값: false)", example = "false")
        Boolean dryRun
) {
    public LoadTestCleanupReq {
        if (dryRun == null) {
            dryRun = false;
        }
    }

    public boolean isDryRun() {
        return dryRun != null && dryRun;
    }
}
