package triplestar.mixchat.domain.report.report.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.report.report.dto.ReportAdminListResp;
import triplestar.mixchat.domain.report.report.dto.ReportStatusUpdateReq;
import triplestar.mixchat.domain.report.report.dto.ReportStatusUpdateResp;
import triplestar.mixchat.domain.report.report.entity.Report;
import triplestar.mixchat.domain.report.report.repository.ReportRepository;
import triplestar.mixchat.domain.report.report.service.ReportAdminService;
import triplestar.mixchat.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class ReportAdminController  {
    private final ReportAdminService reportAdminService;
    private final ReportRepository reportRepository;

    @PatchMapping("/{reportId}")
    public ApiResponse<ReportStatusUpdateResp> updateReportStatus(
            @PathVariable Long reportId,
            @RequestBody @Valid ReportStatusUpdateReq request
    ) {
        Report updated = reportAdminService.updateReportStatus(reportId, request.status());
        return ApiResponse.ok("상태 변경 완료", ReportStatusUpdateResp.from(updated));
    }

    @GetMapping
    public ApiResponse<Page<ReportAdminListResp>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ReportAdminListResp> result = reportAdminService.getReports(page, size);
        return ApiResponse.ok("신고 목록 조회 성공", result);
    }
}
