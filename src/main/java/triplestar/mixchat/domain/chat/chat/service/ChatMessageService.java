package triplestar.mixchat.domain.chat.chat.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.entity.ChatRoom;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;
import triplestar.mixchat.domain.member.member.entity.Member;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage saveMessage(ChatRoom room, Member member, String content, ChatMessage.MessageType messageType) {
        ChatMessage message = ChatMessage.builder()
                .chatRoomId(room.getId())
                .senderId(member.getId())
                .content(content)
                .messageType(messageType)
                .build();
        return chatMessageRepository.save(message);
    }

    public ChatMessage saveFileMessage(ChatRoom room, Member member, String fileUrl, ChatMessage.MessageType messageType) {
        if (messageType != ChatMessage.MessageType.IMAGE && messageType != ChatMessage.MessageType.FILE) {
            throw new IllegalArgumentException("파일 메시지는 IMAGE 또는 FILE 타입이어야 합니다.");
        }
        return saveMessage(room, member, fileUrl, messageType);
    }

    public List<ChatMessage> getMessages(Long roomId) {
        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);
    }
}