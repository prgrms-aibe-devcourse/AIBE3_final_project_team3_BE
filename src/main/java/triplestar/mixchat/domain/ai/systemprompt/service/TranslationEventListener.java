package triplestar.mixchat.domain.ai.systemprompt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import triplestar.mixchat.domain.ai.systemprompt.dto.TranslationReq;

@Component
@RequiredArgsConstructor
public class TranslationEventListener {

    private final AiTranslationService aiTranslationService;

    @Async
    @EventListener
    public void handleTranslationEvent(TranslationReq req) {
        aiTranslationService.handleTranslation(req);
    }
}
