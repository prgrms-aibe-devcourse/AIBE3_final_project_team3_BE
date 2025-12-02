package triplestar.mixchat.domain.ai.systemprompt.service;

import jakarta.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
        // 스프링이 @Order 어노테이션을 기반으로 주입 시 자동 정렬하므로 수동 정렬 제거
        log.info("번역 프로바이더 로드 완료. 순서: {}", translationProviders.stream().map(p -> p.getClass().getSimpleName()).toList());
    }

    public void handleTranslation(TranslationReq req) {
        log.debug("번역 요청 수신: messageId={}", req.chatMessageId());

        // 1. DB에서 원본 메시지를 찾고 Reactive Flow 시작
        Mono.justOrEmpty(chatMessageRepository.findById(req.chatMessageId()))
                .switchIfEmpty(Mono.fromRunnable(() -> 
                    log.error("번역 요청 처리 실패: 메시지를 찾을 수 없습니다. messageId={}", req.chatMessageId())
                ))
                .flatMapMany(chatMessage -> 
                    // 2. 등록된 프로바이더들을 순서대로 시도합니다.
                    Flux.fromIterable(translationProviders)
                            .concatMap(provider -> provider.translate(req.originalContent())
                                    .map(resp -> Map.entry(provider.getClass().getSimpleName(), resp)) // Provider 이름과 결과 묶기
                                    .doOnSubscribe(s -> log.info("'{}'로 번역 시도...", provider.getClass().getSimpleName()))
                                    .onErrorResume(e -> {
                                        log.warn("'{}' 실패. 다음 프로바이더를 시도합니다. 에러: {}", provider.getClass().getSimpleName(), e.getMessage());
                                        return Mono.empty(); // 에러 발생 시 비워서 다음 프로바이더로 넘어감
                                    })
                            )
                            .next() // 첫 번째로 성공한 결과만 취함
                            .doOnNext(entry -> {
                                String providerName = entry.getKey();
                                TranslationResp translationResponse = entry.getValue();
                                String translatedContent = translationResponse.correctedContent();

                                // 3. 번역 결과가 유효한 경우 DB에 저장하고 클라이언트에 알립니다.
                                if (translatedContent != null && !translatedContent.isBlank()) {
                                    log.info("번역 성공 (Provider: {}): messageId={}, translatedText={}", providerName, req.chatMessageId(), translatedContent);
                                    chatMessage.setTranslatedContent(translatedContent);
                                    ChatMessage updatedMessage = chatMessageRepository.save(chatMessage);
                                    notifyClientOfUpdate(updatedMessage, translationResponse);
                                } else {
                                    log.debug("번역이 필요하지 않음 (Provider: {}): messageId={}", providerName, req.chatMessageId());
                                }
                            })
                )
                .subscribe(); // 구독하여 실행
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
