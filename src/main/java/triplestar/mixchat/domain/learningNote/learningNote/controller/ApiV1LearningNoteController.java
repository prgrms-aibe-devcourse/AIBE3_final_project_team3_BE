package triplestar.mixchat.domain.learningNote.learningNote.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteCreateReq;
import triplestar.mixchat.domain.learningNote.learningNote.service.LearningNoteService;
import triplestar.mixchat.global.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/learning/notes")
@RequiredArgsConstructor
@Tag(name = "LearningNote", description = "학습노트 저장/조회 API")
public class ApiV1LearningNoteController {
    private final LearningNoteService learningNoteService;

    @PostMapping("/save")
    @Operation(summary = "학습노트에 생성", description = "번역 결과를 받아 학습노트와 피드백을 함께 저장합니다.")
    public ApiResponse<Long> createForm(
            @RequestBody @Valid LearningNoteCreateReq req
    ) {
        Long learningNoteId = learningNoteService.createWithFeedbacks(req);
        return ApiResponse.ok("학습노트가 저장되었습니다.", learningNoteId);
    }
}
