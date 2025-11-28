package triplestar.mixchat.domain.learningNote.learningNote.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.learningNote.learningNote.constant.LearningFilter;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteCreateReq;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteFeedbackResp;
import triplestar.mixchat.domain.learningNote.learningNote.service.LearningNoteService;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.security.CustomUserDetails;

@RestController
@RequestMapping("/api/v1/learning-notes")
@RequiredArgsConstructor
public class ApiV1LearningNoteController implements ApiLearningNoteController{
    private final LearningNoteService learningNoteService;

    @Override
    @PostMapping
    public CustomResponse<Long> createLearningNote(
            @RequestBody @Valid LearningNoteCreateReq req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long learningNoteId = learningNoteService.createWithFeedbacks(req, user.getId());
        return CustomResponse.ok("학습노트가 저장되었습니다.", learningNoteId);
    }

    @Override
    @GetMapping
    public CustomResponse<Page<LearningNoteFeedbackResp>> getLearningNotes(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam TranslationTagCode tag,
            @RequestParam LearningFilter learningFilter,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Page<LearningNoteFeedbackResp> result = learningNoteService.getLearningNotes(pageable, user.getId(), tag, learningFilter);
        return CustomResponse.ok("학습노트 목록 조회 성공", result);
    }

    @Override
    @PatchMapping("/feedbacks/{feedbackId}/mark/learned")
    public CustomResponse<Void> markLearned(
            @PathVariable Long feedbackId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        learningNoteService.updateFeedbackMark(feedbackId, user.getId(), true);
        return CustomResponse.ok("피드백이 학습 완료로 변경되었습니다.");
    }

    @Override
    @PatchMapping("/feedbacks/{feedbackId}/mark/unlearned")
    public CustomResponse<Void> markUnLearned(
            @PathVariable Long feedbackId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        learningNoteService.updateFeedbackMark(feedbackId, user.getId(), false);
        return CustomResponse.ok("피드백이 학습 미완료로 변경되었습니다.");
    }
}
