package triplestar.mixchat.domain.report.report.dto;

import triplestar.mixchat.domain.report.report.constant.ReportStatus;
import triplestar.mixchat.domain.report.report.entity.Report;

public record ReportStatusUpdateResponse(
        Long id,
        ReportStatus status
) {
    public static ReportStatusUpdateResponse from(Report report) {
        return new ReportStatusUpdateResponse(
                report.getId(),
                report.getStatus()
        );
    }
}