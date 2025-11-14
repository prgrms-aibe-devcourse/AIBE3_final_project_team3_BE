package triplestar.mixchat.domain.report.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;
import triplestar.mixchat.domain.report.report.dto.ReportAdminListResp;
import triplestar.mixchat.domain.report.report.dto.ReportStatusUpdateReq;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.springdoc.CommonBadResponse;
import triplestar.mixchat.global.springdoc.SecurityRequireResponse;
import triplestar.mixchat.global.springdoc.SuccessResponse;

@Tag(name = "ApiV1ReportAdminController", description = "API 관리자 신고 관리 컨트롤러")
@CommonBadResponse
@SuccessResponse
public interface ApiReportAdminController {

    // --- 1. 신고 상태 변경 (PATCH /{reportId}) ---
    @Operation(summary = "신고 상태 변경", description = "관리자가 특정 신고의 상태를 변경합니다. (예: WAITING → APPROVED)")
    @SecurityRequireResponse
    CustomResponse<Void> updateReportStatus(
            @Parameter(description = "신고 ID", example = "101", required = true)
            Long reportId,

            @RequestBody(description = "변경할 신고 상태", required = true)
            @Valid ReportStatusUpdateReq request
    );

    // --- 2. 신고 목록 조회 (GET /) ---
    @Operation(summary = "신고 목록 조회", description = "관리자가 전체 신고 목록을 조회합니다.")
    @SecurityRequireResponse
    CustomResponse<Page<ReportAdminListResp>> getReports(
            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size
    );
}