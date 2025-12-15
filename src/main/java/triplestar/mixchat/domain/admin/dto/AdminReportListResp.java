package triplestar.mixchat.domain.admin.dto;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import triplestar.mixchat.domain.report.constant.ReportCategory;
import triplestar.mixchat.domain.report.constant.ReportStatus;
import triplestar.mixchat.domain.report.entity.Report;

@Schema(description = "관리자용 신고 목록 응답")
public record AdminReportListResp(
        @Schema(description = "신고 ID", example = "1", requiredMode = REQUIRED)
        Long id,

        @Schema(description = "신고 대상 회원 ID", example = "102", requiredMode = REQUIRED)
        Long targetMemberId,

        @Schema(description = "신고된 메시지 내용", example = "욕설이 포함된 메시지입니다.")
        String reportedMsgContent,

        @Schema(description = "신고 상태", example = "WAITING", requiredMode = REQUIRED)
        ReportStatus status,

        @Schema(description = "신고 카테고리", example = "ABUSE", requiredMode = REQUIRED)
        ReportCategory category,

        @Schema(description = "세부 신고 사유", example = "지속적인 욕설을 사용합니다.")
        String reportedReason,

        @Schema(description = "신고 생성일시", example = "2025-11-12T15:30:00", requiredMode = REQUIRED)
        LocalDateTime createdAt
) {
    public static AdminReportListResp from(Report report) {
        return new AdminReportListResp(
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