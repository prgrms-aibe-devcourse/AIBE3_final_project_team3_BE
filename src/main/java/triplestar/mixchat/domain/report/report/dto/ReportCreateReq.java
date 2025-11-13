package triplestar.mixchat.domain.report.report.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import triplestar.mixchat.domain.report.report.constant.ReportCategory;

@Schema(description = "신고 생성 요청 데이터")
public record  ReportCreateReq (
    @NotNull
    @Schema(description = "신고 대상 회원 ID", example = "42")
    Long targetMemberId,

    @NotNull
    @Schema(description = "신고 카테고리 (ABUSE: 욕설/비속어, SCAM: 사기, INAPPROPRIATE: 부적절한 콘텐츠)", example = "ABUSE")
    ReportCategory category,

    @Nullable
    @Schema(description = "신고된 메시지 내용", example = "욕설이 포함된 메시지입니다.")
    String reportedMsgContent,

    @Nullable
    @Schema(description = "세부 신고 사유", example = "지속적인 욕설을 사용합니다.")
    String reportedReason
){}