package triplestar.mixchat.domain.ai.systemprompt.service;

import jakarta.annotation.PostConstruct;
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

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTranslationService {

    private final List<TranslationProvider> translationProviders;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessageSendingOperations messagingTemplate;

    @PostConstruct
    public void init() {
        // 우선순위에 따라 번역 프로바이더(플러그인)를 정렬합니다.
        translationProviders.sort(Comparator.comparingInt(TranslationProvider::getOrder));
        log.info("번역 프로바이더 로드 완료. 순서: {}", translationProviders.stream().map(p -> p.getClass().getSimpleName()).toList());
    }

    public void handleTranslation(TranslationReq req) {
        log.debug("번역 요청 수신: messageId={}", req.chatMessageId());

        // 1. DB에서 원본 메시지를 찾습니다.
        chatMessageRepository.findById(req.chatMessageId())
                .ifPresentOrElse(
                        chatMessage -> {
                            // 2. 등록된 프로바이더들을 순서대로 시도합니다.
                            Flux.fromIterable(translationProviders)
                                    .concatMap(provider -> provider.translate(req.originalContent())
                                            .doOnSubscribe(s -> log.info("'{}'로 번역 시도...", provider.getClass().getSimpleName()))
                                            .onErrorResume(e -> {
                                                log.warn("'{}' 실패. 다음 프로바이더를 시도합니다. 에러: {}", provider.getClass().getSimpleName(), e.getMessage());
                                                return Mono.empty(); // 에러 발생 시 비워서 다음 프로바이더로 넘어감
                                            }))
                                    .next() // 첫 번째로 성공한 결과만 취함
                                    .subscribe(translationResponse -> {
                                        String translatedContent = translationResponse.correctedContent();

                                        // 3. 번역 결과가 유효한 경우 DB에 저장하고 클라이언트에 알립니다.
                                        if (translatedContent != null && !translatedContent.isBlank()) {
                                            log.debug("번역 성공: messageId={}, translatedText={}", req.chatMessageId(), translatedContent);
                                            chatMessage.setTranslatedContent(translatedContent);
                                            ChatMessage updatedMessage = chatMessageRepository.save(chatMessage);
                                            notifyClientOfUpdate(updatedMessage, translationResponse);
                                        } else {
                                            log.debug("번역이 필요하지 않음: messageId={}", req.chatMessageId());
                                        }
                                    });
                        },
                        () -> log.error("번역 요청 처리 실패: 메시지를 찾을 수 없습니다. messageId={}", req.chatMessageId())
                );
    }

    private void notifyClientOfUpdate(ChatMessage updatedMessage, TranslationResp translationResp) {
        String destination = String.format("/topic/%s/rooms/%d/message-update",
                updatedMessage.getChatRoomType().name().toLowerCase(),
                updatedMessage.getChatRoomId());

        // 클라이언트에게 전달할 최종 DTO
        Map<String, Object> payload = Map.of(
                "messageId", updatedMessage.getId(),
                "translatedContent", updatedMessage.getTranslatedContent(),
                "feedback", translationResp.feedback()
        );

        messagingTemplate.convertAndSend(destination, payload);
        log.debug("클라이언트에 번역 완료 알림 전송: destination={}, messageId={}", destination, updatedMessage.getId());
    }
}
