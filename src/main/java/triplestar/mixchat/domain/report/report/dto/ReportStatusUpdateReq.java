package triplestar.mixchat.domain.report.report.dto;

import jakarta.validation.constraints.NotNull;
import triplestar.mixchat.domain.report.report.constant.ReportStatus;

public record ReportStatusUpdateReq(
        @NotNull ReportStatus status
) {}