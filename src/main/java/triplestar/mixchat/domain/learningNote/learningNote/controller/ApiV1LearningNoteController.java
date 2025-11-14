package triplestar.mixchat.domain.learningNote.learningNote.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteCreateReq;
import triplestar.mixchat.domain.learningNote.learningNote.service.LearningNoteService;
import triplestar.mixchat.global.response.CustomResponse;

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
}
