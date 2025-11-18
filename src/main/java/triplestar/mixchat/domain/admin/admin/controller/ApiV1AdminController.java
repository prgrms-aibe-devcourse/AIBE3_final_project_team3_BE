package triplestar.mixchat.domain.admin.admin.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.admin.admin.dto.AdminReportListResp;
import triplestar.mixchat.domain.admin.admin.dto.AdminSentenceGameCreateReq;
import triplestar.mixchat.domain.admin.admin.dto.AdminSentenceGameListResp;
import triplestar.mixchat.domain.admin.admin.service.AdminReportService;
import triplestar.mixchat.domain.admin.admin.service.AdminSentenceGameService;
import triplestar.mixchat.domain.report.report.dto.ReportStatusUpdateReq;
import triplestar.mixchat.domain.report.report.entity.Report;
import triplestar.mixchat.global.response.CustomResponse;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class ApiV1AdminController implements  ApiAdminController {
    private final AdminReportService adminReportService;
    private final AdminSentenceGameService adminSentenceGameService;

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
    public CustomResponse<Page<AdminReportListResp>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AdminReportListResp> result = adminReportService.getReports(page, size);
        return CustomResponse.ok("신고 목록 조회 성공", result);
    }


    @Override
    @PostMapping("/sentence-game")
    public CustomResponse<Long> createMiniGame(
            @RequestBody @Valid AdminSentenceGameCreateReq req
    ) {
        Long sentenceId = adminSentenceGameService.createSentenceGame(req);
        return CustomResponse.ok("미니게임 문장이 등록되었습니다.", sentenceId);
    }

    @Override
    @GetMapping("/sentence-game")
    public CustomResponse<List<AdminSentenceGameListResp>> getMiniGameList() {
        List<AdminSentenceGameListResp> resp = adminSentenceGameService.getList();
        return CustomResponse.ok("미니게임 목록 조회 성공", resp);
    }
}