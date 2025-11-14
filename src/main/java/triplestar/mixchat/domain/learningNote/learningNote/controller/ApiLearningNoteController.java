package triplestar.mixchat.domain.learningNote.learningNote.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteCreateReq;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.springdoc.CommonBadResponse;
import triplestar.mixchat.global.springdoc.SignInInRequireResponse;
import triplestar.mixchat.global.springdoc.SuccessResponse;

@Tag(name = "ApiV1LearningNoteController", description = "API 학습노트 컨트롤러")
@CommonBadResponse
@SuccessResponse
@SecurityRequirement(name = "Authorization")
public interface ApiLearningNoteController {

    // --- 1. 학습노트 생성 (POST /save) ---
    @Operation(summary = "학습노트 생성", description = "번역 결과를 받아 학습노트와 피드백을 함께 저장합니다.")
    @SignInInRequireResponse
    CustomResponse<Long> createLearningNote(
            @RequestBody(description = "학습노트 생성 데이터", required = true)
            @Valid LearningNoteCreateReq req
    );
}