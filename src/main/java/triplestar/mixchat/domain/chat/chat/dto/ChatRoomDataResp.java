package triplestar.mixchat.domain.chat.chat.dto;

import java.util.List;
import lombok.Value;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage.ConversationType;

@Value
public class ChatRoomDataResp {
    ConversationType conversationType;
    List<MessageResp> messages;
}
