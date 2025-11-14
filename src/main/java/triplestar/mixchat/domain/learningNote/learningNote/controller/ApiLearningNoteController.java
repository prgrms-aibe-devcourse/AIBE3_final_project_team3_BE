package triplestar.mixchat.domain.learningNote.learningNote.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import triplestar.mixchat.domain.learningNote.learningNote.constant.LearningStatus;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteCreateReq;
import triplestar.mixchat.domain.learningNote.learningNote.dto.LearningNoteListResp;
import triplestar.mixchat.domain.translation.translation.constant.TranslationTagCode;
import triplestar.mixchat.global.response.ApiResponse;
import triplestar.mixchat.global.springdoc.CommonBadResponse;
import triplestar.mixchat.global.springdoc.SignInInRequireResponse;
import triplestar.mixchat.global.springdoc.SuccessResponse;

@Tag(name = "ApiV1LearningNoteController", description = "API 학습노트 컨트롤러")
@CommonBadResponse
@SuccessResponse
public interface ApiLearningNoteController {

    // --- 1. 학습노트 생성 (POST /save) ---
    @Operation(summary = "학습노트 생성", description = "번역 결과를 받아 학습노트와 피드백을 함께 저장합니다.")
    @SignInInRequireResponse
    ApiResponse<Long> createLearningNote(
            @RequestBody(description = "학습노트 생성 데이터", required = true)
            @Valid LearningNoteCreateReq req
    );

    // --- 2. 학습노트 목록 조회 (GET ) ---
    @Operation(summary = "학습노트 목록 조회", description = "태그와 학습 상태를 기준으로 회원의 학습노트를 조회합니다.")
    @SignInInRequireResponse
    ApiResponse<List<LearningNoteListResp>> getLearningNotes(
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "20")
            int size,
            @Parameter(description = "회원 ID", example = "1")
            @RequestParam
            Long memberId,
            @Parameter(description = "번역 태그", example = "TRANSLATION")
            @RequestParam
            TranslationTagCode tag,
            @Parameter(description = "학습 상태", example = "LEARNED")
            @RequestParam
            LearningStatus status
    );
}