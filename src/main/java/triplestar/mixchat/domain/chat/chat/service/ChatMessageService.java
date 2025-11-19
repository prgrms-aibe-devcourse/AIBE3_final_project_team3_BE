package triplestar.mixchat.domain.chat.chat.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    public MessageResp saveMessage(Long roomId, Long senderId, String senderNickname, String content, ChatMessage.MessageType messageType) {
        // 컨트롤러에서 이미 인가 및 존재 여부를 확인했으므로, 여기서는 검증 로직을 모두 제거합니다.
        ChatMessage message = ChatMessage.builder()
                .chatRoomId(roomId)
                .senderId(senderId)
                .content(content)
                .messageType(messageType)
                .build();
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 파라미터로 받은 닉네임을 사용하여 응답을 생성합니다.
        return MessageResp.from(savedMessage, senderNickname);
    }

    public MessageResp saveFileMessage(Long roomId, Long senderId, String senderNickname, String fileUrl, ChatMessage.MessageType messageType) {
        if (messageType != ChatMessage.MessageType.IMAGE && messageType != ChatMessage.MessageType.FILE) {
            throw new IllegalArgumentException("파일 메시지는 IMAGE 또는 FILE 타입이어야 합니다.");
        }
        // 수정된 saveMessage 메소드를 호출하도록 변경합니다.
        return saveMessage(roomId, senderId, senderNickname, fileUrl, messageType);
    }

    public List<MessageResp> getMessagesWithSenderInfo(Long roomId) {
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId);

        List<Long> senderIds = messages.stream()
                .map(ChatMessage::getSenderId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> senderNames = memberRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));

        return messages.stream()
                .map(message -> {
                    String senderName = senderNames.getOrDefault(message.getSenderId(), "Unknown");
                    return MessageResp.from(message, senderName);
                })
                .collect(Collectors.toList());
    }
}