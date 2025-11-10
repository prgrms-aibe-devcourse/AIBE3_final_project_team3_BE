package triplestar.mixchat.domain.report.report.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.report.report.dto.ReportStatusUpdateRequest;
import triplestar.mixchat.domain.report.report.dto.ReportStatusUpdateResponse;
import triplestar.mixchat.domain.report.report.entity.Report;
import triplestar.mixchat.domain.report.report.service.ReportAdminService;
import triplestar.mixchat.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class ReportAdminController  {
    private final ReportAdminService reportAdminService;

    @PatchMapping("/{reportId}")
    public ResponseEntity<ApiResponse<ReportStatusUpdateResponse>> updateReportStatus(
            @PathVariable Long reportId,
            @RequestBody @Valid ReportStatusUpdateRequest request
    ) {
        Report updated = reportAdminService.updateReportStatus(reportId, request.status());
        return ResponseEntity.ok(
                ApiResponse.ok("상태 변경 완료", ReportStatusUpdateResponse.from(updated))
        );
    }

}
