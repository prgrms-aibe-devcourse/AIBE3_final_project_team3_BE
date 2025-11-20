package triplestar.mixchat.domain.miniGame.sentenceGame.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import triplestar.mixchat.domain.miniGame.sentenceGame.dto.SentenceGameCountResp;
import triplestar.mixchat.domain.miniGame.sentenceGame.dto.SentenceGameStartResp;
import triplestar.mixchat.domain.miniGame.sentenceGame.dto.SentenceGameSubmitReq;
import triplestar.mixchat.domain.miniGame.sentenceGame.dto.SentenceGameSubmitResp;
import triplestar.mixchat.global.response.CustomResponse;
import triplestar.mixchat.global.springdoc.CommonBadResponse;
import triplestar.mixchat.global.springdoc.SignInInRequireResponse;
import triplestar.mixchat.global.springdoc.SuccessResponse;

@Tag(name = "ApiV1SentenceGameController", description = "사용자 문장 미니게임 API")
@CommonBadResponse
@SuccessResponse
@SecurityRequirement(name = "Authorization")
public interface ApiSentenceGameController {
    @Operation(summary = "문장게임 전체 문제 수 조회")
    @SignInInRequireResponse
    CustomResponse<SentenceGameCountResp> getSentenceGameCount();

    @Operation(summary = "문장 미니게임 시작", description = "요청한 개수만큼 랜덤 출제된 문제 목록을 반환합니다.")
    @SignInInRequireResponse
    CustomResponse<SentenceGameStartResp> getStartGame(
            @RequestParam Integer count
    );

    @Operation(summary = "문장 미니게임 정답 제출", description = "문제 ID와 사용자 답변을 제출하여 채점 결과를 반환합니다.")
    @SignInInRequireResponse
    CustomResponse<SentenceGameSubmitResp> checkSubmit(
            @RequestBody @Valid SentenceGameSubmitReq req
    );
}