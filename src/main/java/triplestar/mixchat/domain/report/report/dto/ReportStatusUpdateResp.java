package triplestar.mixchat.domain.report.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import triplestar.mixchat.domain.report.report.constant.ReportStatus;
import triplestar.mixchat.domain.report.report.entity.Report;

@Schema(description = "신고 상태 변경 응답")
public record ReportStatusUpdateResp(
        @NotNull
        @Schema(description = "신고 ID", example = "101")
        Long id,

        @NotNull
        @Schema(description = "변경된 신고 상태", example = "APPROVED")
        ReportStatus status
) {
    public static ReportStatusUpdateResp from(Report report) {
        return new ReportStatusUpdateResp(
                report.getId(),
                report.getStatus()
        );
    }
}