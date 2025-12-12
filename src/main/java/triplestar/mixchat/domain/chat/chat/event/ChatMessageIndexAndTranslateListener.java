package triplestar.mixchat.domain.chat.chat.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import triplestar.mixchat.domain.ai.systemprompt.dto.TranslationReq;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage.MessageType;
import triplestar.mixchat.domain.chat.search.event.MessageIndexEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageIndexAndTranslateListener {

    private final ApplicationEventPublisher eventPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatMessageCreatedEvent event) {
        if (event.messageType() == null) {
            return;
        }

        // Elasticsearch 인덱싱
        if (event.messageType() == MessageType.TEXT) {
            eventPublisher.publishEvent(new MessageIndexEvent(
                    event.messageId(),
                    event.roomId(),
                    event.chatRoomType(),
                    event.senderId(),
                    event.senderNickname(),
                    event.content(),
                    event.sequence(),
                    event.createdAt()
            ));
        }

        // 번역 요청 (TEXT + 사용자가 번역을 켠 경우)
        if (event.translateEnabled() && event.messageType() == MessageType.TEXT) {
            eventPublisher.publishEvent(new TranslationReq(event.messageId(), event.content()));
        }
    }
}
