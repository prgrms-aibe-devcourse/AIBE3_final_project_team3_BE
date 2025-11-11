package triplestar.mixchat.domain.report.report.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.report.report.dto.ReportCreateRequest;
import triplestar.mixchat.domain.report.report.service.ReportService;
import triplestar.mixchat.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createReport(
            @RequestBody @Valid ReportCreateRequest request
    ) {
        reportService.createReport(request);

        return ResponseEntity.ok(
                ApiResponse.ok("신고가 완료되었습니다", null)
        );
    }
}
