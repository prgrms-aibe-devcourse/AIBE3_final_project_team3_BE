package triplestar.mixchat.domain.report.report.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.report.report.dto.ReportCreateReq;
import triplestar.mixchat.domain.report.report.service.ReportService;
import triplestar.mixchat.global.response.CustomResponse;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ApiV1ReportController implements ApiReportController {
    private final ReportService reportService;

    @Override
    @PostMapping
    public CustomResponse<Long> createReport(
            @RequestBody @Valid ReportCreateReq request
    ) {
        Long reportId = reportService.createReport(request);

        return CustomResponse.ok("신고가 완료되었습니다", reportId);
    }
}