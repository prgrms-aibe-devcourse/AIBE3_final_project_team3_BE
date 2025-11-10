package triplestar.mixchat.domain.report.report.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.report.report.constant.ReportCategory;

@Getter
@NoArgsConstructor
public class ReportCreateRequest {
    @NotNull
    private Long targetMemberId;

    @NotNull
    private ReportCategory category;

    private String targetMsgContent;

    private String reasonText;
}