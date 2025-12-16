package triplestar.mixchat.domain.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import triplestar.mixchat.domain.report.constant.ReportStatus;

@Schema(description = "신고 상태 변경 요청")
public record ReportStatusUpdateReq(
        @NotNull
        @Schema(description = "변경할 신고 상태", example = "APPROVED")
        ReportStatus status
) {}