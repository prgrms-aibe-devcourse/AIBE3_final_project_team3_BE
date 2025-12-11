package triplestar.mixchat.domain.chat.search.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import triplestar.mixchat.domain.chat.search.document.ChatMessageDocument;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageIndexEventListener {

    private final ElasticsearchTemplate elasticsearchTemplate;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessageIndexEvent(MessageIndexEvent event) {
        try {
            ChatMessageDocument document = new ChatMessageDocument(
                    event.messageId(),
                    event.chatRoomId(),
                    event.chatRoomType(),
                    event.senderId(),
                    event.senderNickname(),
                    event.content(),
                    null, // translatedContent - 나중에 업데이트됨
                    event.sequence(),
                    event.createdAt()
            );

            elasticsearchTemplate.save(document);
            log.debug("메시지 Elasticsearch 인덱싱 완료 - messageId={}", event.messageId());
        } catch (Exception e) {
            log.warn("메시지 Elasticsearch 인덱싱 실패 - messageId={}, reason={}", event.messageId(), e.getMessage());
        }
    }
}
