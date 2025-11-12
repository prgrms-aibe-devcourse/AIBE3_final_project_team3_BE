package triplestar.mixchat.domain.report.report.dto;


import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.report.report.constant.ReportCategory;

@Getter
@NoArgsConstructor
public class ReportCreateReq {
    @NotNull
    private Long targetMemberId;

    @NotNull
    private ReportCategory category;

    @Nullable
    private String reportedMsgContent;

    @Nullable
    private String reportedReason;
}