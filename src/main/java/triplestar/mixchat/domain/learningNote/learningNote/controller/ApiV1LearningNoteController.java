package triplestar.mixchat.domain.learningNote.learningNote.controller;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.learningNote.learningNote.constant.LearningFilter;
import triplestar.mixchat.domain.learningNote.learningNote.dto.FeedbackMarkUpdateReq;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteCreateReq;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteListResp;
import triplestar.mixchat.domain.learningNote.learningNote.service.LearningNoteService;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.springdoc.SignInInRequireResponse;

@RestController
@RequestMapping("/api/v1/learning/notes")
@RequiredArgsConstructor
public class ApiV1LearningNoteController implements ApiLearningNoteController{
    private final LearningNoteService learningNoteService;

    @Override
    @PostMapping
    public CustomResponse<Long> createLearningNote(
            @RequestBody @Valid LearningNoteCreateReq req
    ) {
        Long learningNoteId = learningNoteService.createWithFeedbacks(req);
        return CustomResponse.ok("학습노트가 저장되었습니다.", learningNoteId);
    }

    @Override
    @GetMapping
    public CustomResponse<Page<LearningNoteListResp>> getLearningNotes(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam Long memberId,
            @RequestParam TranslationTagCode tag,
            @RequestParam LearningFilter learningFilter
    ) {
        Page<LearningNoteListResp> result = learningNoteService.getLearningNotes(pageable, memberId, tag, learningFilter);
        return CustomResponse.ok("학습노트 목록 조회 성공", result);
    }

    @Override
    @PatchMapping("/feedbacks/{feedbackId}/mark")
    public CustomResponse<Void> updateFeedbackMark(
            @PathVariable Long feedbackId,
            @RequestBody @Valid FeedbackMarkUpdateReq req
    ) {
        learningNoteService.updateFeedbackMark(feedbackId, req.marked());
        return CustomResponse.ok("피드백 학습 상태가 변경되었습니다.");
    }
}
