package triplestar.mixchat.domain.report.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import triplestar.mixchat.domain.report.constant.ReportStatus;
import triplestar.mixchat.domain.report.entity.Report;

@Schema(description = "신고 상태 변경 응답")
public record ReportStatusUpdateResp(
        @Schema(description = "신고 ID", example = "101", requiredMode = REQUIRED)
        Long id,

        @Schema(description = "변경된 신고 상태", example = "APPROVED", requiredMode = REQUIRED)
        ReportStatus status
) {
    public static ReportStatusUpdateResp from(Report report) {
        return new ReportStatusUpdateResp(
                report.getId(),
                report.getStatus()
        );
    }
}