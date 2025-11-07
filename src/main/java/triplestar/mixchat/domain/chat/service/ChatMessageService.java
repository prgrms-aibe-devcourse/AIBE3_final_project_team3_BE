package triplestar.mixchat.domain.chat.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.entity.ChatRoom;
import triplestar.mixchat.domain.chat.repository.ChatMessageRepository;
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

    /*
    private final ActiveServiceService activeServiceService;

    public ChatMessage savePaymentRequest(ChatRoom room, Member member, MessageRequest messageRequest) {
        ChatMessage message = ChatMessage.builder()
                .room(room)
                .member(member)
                .content(messageRequest.getContent())
                .messageType(ChatMessage.MessageType.PAYMENT_REQUEST)
                .amount(messageRequest.getAmount())
                .memo(messageRequest.getMemo())
                .serviceId(messageRequest.getServiceId())
                .build();
        return chatMessageRepository.save(message);
    }

    public ChatMessage saveMeetingRequest(ChatRoom room, Member member, MessageRequest messageRequest) {
        ChatMessage message = ChatMessage.builder()
                .room(room)
                .member(member)
                .content(messageRequest.getContent())
                .messageType(ChatMessage.MessageType.MEETING_REQUEST)
                .memo(messageRequest.getMemo())
                .build();
        return chatMessageRepository.save(message);
    }

    public ChatMessage saveWorkCompleteRequest(ChatRoom room, Member member, MessageRequest messageRequest) {
        ChatMessage message = ChatMessage.builder()
                .room(room)
                .member(member)
                .content("작업이 완료되었습니다. 확인 요청드립니다!")
                .messageType(ChatMessage.MessageType.WORK_COMPLETE_REQUEST)
                .serviceId(messageRequest.getServiceId())
                .build();
        return chatMessageRepository.save(message);
    }
    
    @Transactional
    public void confirmWorkComplete(Member member, Long serviceId) {
        // ActiveService의 상태 변경 로직만 수행
        activeServiceService.updateActiveServiceStatus(serviceId, member.getEmail());
    }
    */
}
