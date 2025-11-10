package triplestar.mixchat.domain.report.report.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.report.report.dto.ReportAdminListResponse;
import triplestar.mixchat.domain.report.report.dto.ReportCreateRequest;
import triplestar.mixchat.domain.report.report.entity.Report;
import triplestar.mixchat.domain.report.report.repository.ReportRepository;
import triplestar.mixchat.domain.report.report.service.ReportService;
import triplestar.mixchat.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final ReportRepository reportRepository;

    @PostMapping()
    public ResponseEntity<ApiResponse<Long>> createReport(
            @RequestBody @Valid ReportCreateRequest request
    ) {
        reportService.createReport(request);

        return ResponseEntity.ok(
                ApiResponse.ok("신고가 완료되었습니다", null)
        );
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<ReportAdminListResponse>>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Report> reports = reportRepository.findAll(pageable);

        // Report → DTO 변환
        var result = reports.map(ReportAdminListResponse::from);

        return ResponseEntity.ok(
                ApiResponse.ok("신고 목록 조회 성공", result)
        );
    }
}
