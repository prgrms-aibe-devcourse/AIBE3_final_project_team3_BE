package triplestar.mixchat.domain.chat.search.document;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;

@Getter
@NoArgsConstructor
@Document(indexName = "chat_messages")
public class ChatMessageDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private String messageId;

    @Field(type = FieldType.Long)
    private Long chatRoomId;

    @Field(type = FieldType.Keyword)
    private ChatRoomType chatRoomType;

    @Field(type = FieldType.Long)
    private Long senderId;

    @Field(type = FieldType.Text)
    private String senderName;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Text)
    private String translatedContent;

    @Field(type = FieldType.Long)
    private Long sequence;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    public ChatMessageDocument(
            String messageId,
            Long chatRoomId,
            ChatRoomType chatRoomType,
            Long senderId,
            String senderName,
            String content,
            String translatedContent,
            Long sequence,
            LocalDateTime createdAt
    ) {
        this.messageId = messageId;
        this.chatRoomId = chatRoomId;
        this.chatRoomType = chatRoomType;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.translatedContent = translatedContent;
        this.sequence = sequence;
        this.createdAt = createdAt;
    }

    public static ChatMessageDocument fromEntity(ChatMessage message, String senderName) {
        return new ChatMessageDocument(
                message.getId(),
                message.getChatRoomId(),
                message.getChatRoomType(),
                message.getSenderId(),
                senderName,
                message.getContent(),
                message.getTranslatedContent(),
                message.getSequence(),
                message.getCreatedAt()
        );
    }

    public ChatMessageDocument withTranslation(String translatedContent) {
        return new ChatMessageDocument(
                this.messageId,
                this.chatRoomId,
                this.chatRoomType,
                this.senderId,
                this.senderName,
                this.content,
                translatedContent,
                this.sequence,
                this.createdAt
        );
    }
}
