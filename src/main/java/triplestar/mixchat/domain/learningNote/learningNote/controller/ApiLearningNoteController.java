package triplestar.mixchat.domain.learningNote.learningNote.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import triplestar.mixchat.domain.learningNote.learningNote.constant.LearningFilter;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteCreateReq;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteFeedbackResp;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.security.CustomUserDetails;
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
            @Valid
            LearningNoteCreateReq req,
            @AuthenticationPrincipal
            CustomUserDetails user
    );

    // --- 2. 학습노트 목록 조회 (GET ) ---
    @Operation(summary = "학습노트 목록 조회", description = "태그와 학습 상태를 기준으로 회원의 학습노트를 조회합니다.")
    @SignInInRequireResponse
    CustomResponse<Page<LearningNoteFeedbackResp>> getLearningNotes(
            @Parameter(description = "페이지 정보")
            Pageable pageable,
            @Parameter(description = "번역 태그", example = "TRANSLATION")
            @RequestParam
            TranslationTagCode tag,
            @Parameter(description = "학습 상태", example = "LEARNED")
            @RequestParam
            LearningFilter learningFilter,
            @AuthenticationPrincipal
            CustomUserDetails user
    );

    // --- 3-1. 피드백 상태 변경 (PATCH) ---
    @Operation(summary = "피드백 학습 완료로 변경", description = "해당 피드백을 학습 완료 상태로 변경합니다.")
    @SignInInRequireResponse
    CustomResponse<Void> markLearned(
            @Parameter(description = "피드백 ID", example = "1")
            @PathVariable
            Long feedbackId,
            @AuthenticationPrincipal
            CustomUserDetails user
    );

    // --- 3-2. 피드백 상태 변경 (PATCH) ---
    @Operation(summary = "피드백 학습 미완료로 변경", description = "해당 피드백을 학습 미완료 상태로 변경합니다.")
    @SignInInRequireResponse
    CustomResponse<Void> markUnLearned(
            @Parameter(description = "피드백 ID", example = "1")
            @PathVariable
            Long feedbackId,
            @AuthenticationPrincipal
            CustomUserDetails user
    );
}