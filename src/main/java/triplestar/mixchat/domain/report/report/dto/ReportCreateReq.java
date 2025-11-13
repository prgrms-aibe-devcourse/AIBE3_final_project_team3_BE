package triplestar.mixchat.domain.report.report.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import triplestar.mixchat.domain.report.report.constant.ReportCategory;

@Schema(description = "신고 생성 요청 데이터")
@Getter
@NoArgsConstructor
public class ReportCreateReq {
    @NotNull
    @Schema(description = "신고 대상 회원 ID", example = "42")
    private Long targetMemberId;

    @NotNull
    @Schema(description = "신고 카테고리", example = "ABUSE")
    private ReportCategory category;

    @Nullable
    @Schema(description = "신고된 메시지 내용", example = "욕설이 포함된 메시지입니다.")
    private String reportedMsgContent;

    @Nullable
    @Schema(description = "세부 신고 사유", example = "지속적인 욕설을 사용합니다.")
    private String reportedReason;
}