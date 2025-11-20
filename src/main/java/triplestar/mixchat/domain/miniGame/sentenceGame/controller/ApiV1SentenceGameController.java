package triplestar.mixchat.domain.miniGame.sentenceGame.controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import triplestar.mixchat.domain.miniGame.sentenceGame.dto.SentenceGameCountResp;
import triplestar.mixchat.domain.miniGame.sentenceGame.dto.SentenceGameStartResp;
import triplestar.mixchat.domain.miniGame.sentenceGame.dto.SentenceGameSubmitReq;
import triplestar.mixchat.domain.miniGame.sentenceGame.dto.SentenceGameSubmitResp;
import triplestar.mixchat.domain.miniGame.sentenceGame.service.SentenceGameService;
import triplestar.mixchat.global.response.CustomResponse;

@RestController
@RequestMapping("/api/v1/sentence-game")
@RequiredArgsConstructor
public class ApiV1SentenceGameController implements ApiSentenceGameController{
    private final SentenceGameService sentenceGameService;

    @Override
    @GetMapping
    public CustomResponse<SentenceGameCountResp> getSentenceGameCount() {
        long count = sentenceGameService.getTotalCount();
        return CustomResponse.ok("문장 게임 전체 문제 수 조회 성공", SentenceGameCountResp.from(count));
    }

    @Override
    @GetMapping("/start")
    public CustomResponse<SentenceGameStartResp> getStartGame(
            @RequestParam Integer count
    ) {
        SentenceGameStartResp resp = sentenceGameService.startGame(count);
        return CustomResponse.ok("게임 문제가 생성되었습니다.", resp);
    }

    @Override
    @PostMapping("/submit")
    public CustomResponse<SentenceGameSubmitResp> checkSubmit(
            @RequestBody @Valid SentenceGameSubmitReq req
    ) {
        SentenceGameSubmitResp resp = sentenceGameService.submitAnswer(req);
        return CustomResponse.ok("정답 확인 완료", resp);
    }
}
