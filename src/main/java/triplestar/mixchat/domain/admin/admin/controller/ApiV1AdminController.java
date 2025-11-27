package triplestar.mixchat.domain.admin.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import triplestar.mixchat.domain.admin.admin.dto.AdminSentenceGameCreateResp;
import triplestar.mixchat.domain.admin.admin.dto.AdminSentenceGameNoteResp;
import triplestar.mixchat.domain.admin.admin.dto.AdminSentenceGameResp;
import triplestar.mixchat.domain.admin.admin.service.AdminChatRoomService;
import triplestar.mixchat.domain.admin.admin.service.AdminReportService;
import triplestar.mixchat.domain.admin.admin.service.AdminSentenceGameService;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.report.report.dto.ReportStatusUpdateReq;
import triplestar.mixchat.domain.report.report.entity.Report;
import triplestar.mixchat.global.response.CustomResponse;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class ApiV1AdminController implements  ApiAdminController {
    private final AdminReportService adminReportService;
    private final AdminSentenceGameService adminSentenceGameService;
    private final AdminChatRoomService adminChatRoomService;

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
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminReportListResp> result = adminReportService.getReports(pageable);
        return CustomResponse.ok("신고 목록 조회 성공", result);
    }


    @Override
    @PostMapping("/sentence-game")
    public CustomResponse<AdminSentenceGameCreateResp> createSentenceGame(
            @RequestBody @Valid AdminSentenceGameCreateReq req
    ) {
        Long sentenceId = adminSentenceGameService.createSentenceGame(req);
        return CustomResponse.ok("미니게임 문장이 등록되었습니다.", AdminSentenceGameCreateResp.from(sentenceId));
    }

    @Override
    @GetMapping("/sentence-game/notes")
    public CustomResponse<Page<AdminSentenceGameNoteResp>> getSentenceGameNoteList(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminSentenceGameNoteResp> resp = adminSentenceGameService.getSentenceGameNoteList(pageable);

        return CustomResponse.ok("미니게임 등록용 학습노트 목록 조회 성공", resp);
    }

    @Override
    @GetMapping("/sentence-game")
    public CustomResponse<Page<AdminSentenceGameResp>> getSentenceGameList(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminSentenceGameResp> resp = adminSentenceGameService.getSentenceGameList(pageable);

        return CustomResponse.ok("문장게임 목록 조회 성공", resp);
    }

    @Override
    @DeleteMapping("/sentence-game/{sentenceGameId}")
    public CustomResponse<Void> deleteSentenceGame(
            @PathVariable Long sentenceGameId
    ) {
        adminSentenceGameService.deleteSentenceGame(sentenceGameId);
        return CustomResponse.ok("문장게임 문장이 삭제되었습니다.");
    }

    @DeleteMapping("/chat-rooms/{roomId}")
    public CustomResponse<Void> closeChatRoom(
            @PathVariable Long roomId,
            @RequestParam ChatMessage.chatRoomType chatRoomType
    ) {
        adminChatRoomService.forceCloseRoom(roomId, chatRoomType);
        return CustomResponse.ok("채팅방 강제 폐쇄 완료");
    }
}