package triplestar.mixchat.domain.report.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import triplestar.mixchat.domain.report.report.dto.ReportCreateReq;
import triplestar.mixchat.global.response.ApiResponse;
import triplestar.mixchat.global.springdoc.CommonBadResponse;
import triplestar.mixchat.global.springdoc.SignInInRequireResponse;
import triplestar.mixchat.global.springdoc.SuccessResponse;

@Tag(name = "ApiV1ReportController", description = "API 신고 컨트롤러")
@CommonBadResponse
@SuccessResponse
public interface ApiReportController {

    // --- 1. 신고 생성 (POST /) ---
    @Operation(summary = "신고 생성",description = "사용자가 다른 사용자를 신고합니다.")
    @SignInInRequireResponse
    ApiResponse<Long> createReport(
            @RequestBody(description = "신고 생성 요청 데이터", required = true)
            @Valid ReportCreateReq request
    );
}