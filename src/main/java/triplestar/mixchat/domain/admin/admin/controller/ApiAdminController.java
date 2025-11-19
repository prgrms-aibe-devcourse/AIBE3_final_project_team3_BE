package triplestar.mixchat.domain.admin.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import triplestar.mixchat.domain.admin.admin.dto.AdminReportListResp;
import triplestar.mixchat.domain.admin.admin.dto.AdminSentenceGameCreateReq;
import triplestar.mixchat.domain.admin.admin.dto.AdminSentenceGameCreateResp;
import triplestar.mixchat.domain.admin.admin.dto.AdminSentenceGameNoteResp;
import triplestar.mixchat.domain.report.report.dto.ReportStatusUpdateReq;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.springdoc.CommonBadResponse;
import triplestar.mixchat.global.springdoc.SecurityRequireResponse;
import triplestar.mixchat.global.springdoc.SuccessResponse;

@Tag(name = "ApiV1AdminController", description = "API 관리자 컨트롤러")
@CommonBadResponse
@SuccessResponse
@SecurityRequirement(name = "Authorization")
public interface ApiAdminController {
    // --- 1. 신고 상태 변경 (PATCH /reports/{reportId}) ---
    @Operation(summary = "신고 상태 변경", description = "관리자가 특정 신고의 상태를 변경합니다. (예: WAITING → APPROVED)")
    @SecurityRequireResponse
    CustomResponse<Void> updateReportStatus(
            @Parameter(description = "신고 ID", example = "101", required = true)
            Long reportId,

            @RequestBody(description = "변경할 신고 상태", required = true)
            @Valid ReportStatusUpdateReq request
    );

    // --- 2. 신고 목록 조회 (GET /reports) ---
    @Operation(summary = "신고 목록 조회", description = "관리자가 전체 신고 목록을 조회합니다.")
    @SecurityRequireResponse
    CustomResponse<Page<AdminReportListResp>> getReports(
            @Parameter(description = "페이지 정보")
            Pageable pageable
    );

    // --- 3. 문장 등록 (POST /sentence-game) ---
    @Operation(summary = "미니게임 문장 등록", description = "관리자가 미니게임에 문장을 등록합니다.")
    @SecurityRequireResponse
    CustomResponse<AdminSentenceGameCreateResp> createSentenceGame(
            @Valid AdminSentenceGameCreateReq req
    );

    // --- 4. 학습노트 조회 (GET /sentence-game) ---
    @Operation(summary = "문장 등록을 위한 학습노트 조회", description = "관리자가 문장 등록을 위한 학습노트 목록을 조회합니다.")
    @SecurityRequireResponse
    CustomResponse<Page<AdminSentenceGameNoteResp>> getSentenceGameNoteList(
            @Parameter(description = "페이지 정보")
            Pageable pageable
    );
}