package triplestar.mixchat.domain.ai.systemprompt.service;

import reactor.core.publisher.Mono;
import triplestar.mixchat.domain.ai.systemprompt.dto.TranslationResp;

public interface TranslationProvider {
    Mono<TranslationResp> translate(String originalContent);
}
