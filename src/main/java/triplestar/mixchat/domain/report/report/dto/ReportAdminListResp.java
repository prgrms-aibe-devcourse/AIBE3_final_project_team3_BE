package triplestar.mixchat.domain.report.report.dto;

import java.time.LocalDateTime;
import triplestar.mixchat.domain.report.report.constant.ReportCategory;
import triplestar.mixchat.domain.report.report.constant.ReportStatus;
import triplestar.mixchat.domain.report.report.entity.Report;

public record ReportAdminListResp(
        Long id,
        Long targetMemberId,
        String reportedMsgContent,
        ReportStatus status,
        ReportCategory category,
        String reportedReason,
        LocalDateTime createdAt
) {
    public static ReportAdminListResp from(Report report) {
        return new ReportAdminListResp(
                report.getId(),
                report.getTargetMemberId(),
                report.getReportedMsgContent(),
                report.getStatus(),
                report.getCategory(),
                report.getReportedReason(),
                report.getCreatedAt()
        );
    }
}