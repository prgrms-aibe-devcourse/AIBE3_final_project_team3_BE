package triplestar.mixchat.domain.ai.systemprompt.service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.ai.systemprompt.dto.TranslationReq;
import triplestar.mixchat.domain.ai.systemprompt.dto.TranslationResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTranslationService {

    private final List<TranslationProvider> translationProviders;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    @PostConstruct
    public void init() {
        log.info("번역 프로바이더 로드 완료. 순서: {}", translationProviders.stream().map(p -> p.getClass().getSimpleName()).toList());
    }

    @Async
    @EventListener
    public void handleTranslation(TranslationReq req) {
        log.debug("번역 요청 수신: messageId={}", req.chatMessageId());

        // 1. 메시지 조회
        ChatMessage chatMessage = chatMessageRepository.findById(req.chatMessageId())
                .orElse(null);

        if (chatMessage == null) {
            log.error("번역 요청 처리 실패: 메시지를 찾을 수 없습니다. messageId={}", req.chatMessageId());
            return;
        }

        // 2. 등록된 프로바이더들을 순서대로 시도 (fallback 패턴)
        for (TranslationProvider provider : translationProviders) {
            String providerName = provider.getClass().getSimpleName();

            try {
                log.info("'{}'로 번역 시도...", providerName);

                TranslationResp translationResp = provider.translate(req.originalContent())
                        .block(java.time.Duration.ofSeconds(30));

                if (translationResp == null) {
                    log.warn("'{}' 응답 없음. 다음 프로바이더를 시도합니다.", providerName);
                    continue;
                }

                String translatedContent = translationResp.correctedContent();

                if (translatedContent != null && !translatedContent.isBlank()) {
                    log.info("번역 성공 (Provider: {}): messageId={}, translatedText={}", providerName, req.chatMessageId(), translatedContent);
                    chatMessage.setTranslatedContent(translatedContent);
                    ChatMessage updatedMessage = chatMessageRepository.save(chatMessage);
                    notifyClientOfUpdate(updatedMessage, translationResp);
                    return;
                } else {
                    log.debug("번역이 필요하지 않음 (Provider: {}): messageId={}", providerName, req.chatMessageId());
                    return;
                }

            } catch (Exception e) {
                log.warn("'{}' 실패. 다음 프로바이더를 시도합니다. 에러: {}", providerName, e.getMessage());
                // 다음 프로바이더로 계속
            }
        }

        log.warn("모든 번역 프로바이더 실패: messageId={}", req.chatMessageId());
    }

    private void notifyClientOfUpdate(ChatMessage updatedMessage, TranslationResp translationResp) {
        String destination = String.format("/topic/%s/rooms/%d",
                updatedMessage.getChatRoomType().name().toLowerCase(),
                updatedMessage.getChatRoomId());

        // 클라이언트에게 전달할 최종 DTO
        Map<String, Object> payload = Map.of(
                "type", "TRANSLATION_UPDATE",
                "messageId", updatedMessage.getId(),
                "translatedContent", updatedMessage.getTranslatedContent(),
                "feedback", translationResp.feedback()
        );

        messagingTemplate.convertAndSend(destination, payload);
        log.debug("클라이언트에 번역 완료 알림 전송: destination={}, messageId={}", destination, updatedMessage.getId());
    }
}
