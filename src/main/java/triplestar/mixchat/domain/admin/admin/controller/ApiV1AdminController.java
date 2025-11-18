package triplestar.mixchat.domain.admin.admin.controller;

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
import triplestar.mixchat.domain.admin.admin.dto.ReportAdminListResp;
import triplestar.mixchat.domain.admin.admin.service.AdminReportService;
import triplestar.mixchat.domain.report.report.dto.ReportStatusUpdateReq;
import triplestar.mixchat.domain.report.report.entity.Report;
import triplestar.mixchat.global.response.CustomResponse;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class ApiV1AdminController implements  ApiAdminController {
    private final AdminReportService adminReportService;

    @Override
    @PatchMapping("/reports/{reportId}")
    public CustomResponse<Void> updateReportStatus(
            @PathVariable Long reportId,
            @RequestBody @Valid ReportStatusUpdateReq request
    ) {
        Report updated = adminReportService.updateReportStatus(reportId, request.status());
        return CustomResponse.ok("상태 변경 완료");
    }

    @Override
    @GetMapping("/reports")
    public CustomResponse<Page<ReportAdminListResp>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ReportAdminListResp> result = adminReportService.getReports(page, size);
        return CustomResponse.ok("신고 목록 조회 성공", result);
    }
}