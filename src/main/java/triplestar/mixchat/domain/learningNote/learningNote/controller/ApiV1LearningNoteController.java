package triplestar.mixchat.domain.learningNote.learningNote.controller;


import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.learningNote.learningNote.constant.LearningStatus;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteCreateReq;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteListResp;
import triplestar.mixchat.domain.learningNote.learningNote.service.LearningNoteService;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;
import triplestar.mixchat.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/learning/notes")
@RequiredArgsConstructor
public class ApiV1LearningNoteController implements ApiLearningNoteController{
    private final LearningNoteService learningNoteService;

    @Override
    @PostMapping
    public ApiResponse<Long> createLearningNote(
            @RequestBody @Valid LearningNoteCreateReq req
    ) {
        Long learningNoteId = learningNoteService.createWithFeedbacks(req);
        return ApiResponse.ok("학습노트가 저장되었습니다.", learningNoteId);
    }

    @Override
    @GetMapping
    public ApiResponse<List<LearningNoteListResp>> getLearningNotes(
            int page,
            int size,
            Long memberId,
            TranslationTagCode tag,
            LearningStatus status
    ) {
        List<LearningNoteListResp> result = learningNoteService.getLearningNotes(page, size, memberId, tag, status);
        return ApiResponse.ok("학습노트 목록 조회 성공", result);
    }
}
