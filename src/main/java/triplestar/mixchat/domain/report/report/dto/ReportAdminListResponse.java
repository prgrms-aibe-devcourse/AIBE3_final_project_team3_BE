package triplestar.mixchat.domain.report.report.dto;

import java.time.LocalDateTime;
import triplestar.mixchat.domain.report.report.constant.ReportCategory;
import triplestar.mixchat.domain.report.report.constant.ReportStatus;
import triplestar.mixchat.domain.report.report.entity.Report;

public record  ReportAdminListResponse (
        Long id,
        Long targetMemberId,
        String targetMsgContent,
        ReportStatus status,
        ReportCategory category,
        String reasonText,
        LocalDateTime createdAt
) {
    public static ReportAdminListResponse from(Report report) {
        return new ReportAdminListResponse(
                report.getId(),
                report.getTargetMemberId(),
                report.getTargetMsgContent(),
                report.getStatus(),
                report.getCategory(),
                report.getReasonText(),
                report.getCreatedAt()
        );
    }
}