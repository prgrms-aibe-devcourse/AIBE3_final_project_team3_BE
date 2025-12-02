package triplestar.mixchat.domain.ai.systemprompt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import triplestar.mixchat.domain.ai.systemprompt.dto.TranslationResp;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(3)
public class GeminiTranslationProvider implements TranslationProvider {

    // TODO:'spring-ai-google-gemini-starter' 의존성 추가 시 주석 해제 및 구현

    @Override
    public Mono<TranslationResp> translate(String originalContent) {
        // 실제 구현 시 Gemini API 호출 로직 추가, 의존성 추가 필요
        log.debug("GeminiTranslationProvider 호출됨 (현재 기능 미구현)");
        return Mono.empty();
    }
}