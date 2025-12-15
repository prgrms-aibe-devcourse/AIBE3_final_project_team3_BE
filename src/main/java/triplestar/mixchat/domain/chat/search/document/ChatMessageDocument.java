package triplestar.mixchat.domain.chat.search.document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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

    // ES에 저장된 날짜 포맷이 혼재되어 있어(yyyy-MM-dd, yyyy-MM-dd'T'HH:mm:ss) String으로 받아 직접 파싱
    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd||yyyy-MM-dd'T'HH:mm:ss||strict_date_optional_time")
    private String createdAt;

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
        this.createdAt = createdAt != null ? createdAt.toString() : null;
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
        ChatMessageDocument doc = new ChatMessageDocument();
        doc.messageId = this.messageId;
        doc.chatRoomId = this.chatRoomId;
        doc.chatRoomType = this.chatRoomType;
        doc.senderId = this.senderId;
        doc.senderName = this.senderName;
        doc.content = this.content;
        doc.translatedContent = translatedContent;
        doc.sequence = this.sequence;
        doc.createdAt = this.createdAt;
        return doc;
    }
    
    public LocalDateTime getCreatedAt() {
        if (createdAt == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(createdAt);
        } catch (DateTimeParseException e) {
            return LocalDate.parse(createdAt).atStartOfDay();
        }
    }
}