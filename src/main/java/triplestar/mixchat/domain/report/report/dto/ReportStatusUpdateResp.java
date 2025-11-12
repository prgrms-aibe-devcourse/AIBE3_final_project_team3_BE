package triplestar.mixchat.domain.report.report.dto;

import triplestar.mixchat.domain.report.report.constant.ReportStatus;
import triplestar.mixchat.domain.report.report.entity.Report;

public record ReportStatusUpdateResp(
        Long id,
        ReportStatus status
) {
    public static ReportStatusUpdateResp from(Report report) {
        return new ReportStatusUpdateResp(
                report.getId(),
                report.getStatus()
        );
    }
}