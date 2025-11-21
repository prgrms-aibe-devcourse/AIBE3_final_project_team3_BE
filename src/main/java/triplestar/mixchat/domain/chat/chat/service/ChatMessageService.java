package triplestar.mixchat.domain.chat.chat.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.notification.constant.NotificationType;
import triplestar.mixchat.global.notifiaction.NotificationEvent;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatInteractionService chatInteractionService;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public MessageResp saveMessage(Long roomId, Long senderId, String senderNickname, String content, ChatMessage.MessageType messageType, ChatMessage.chatRoomType chatRoomType) {
        // 1. 사용자가 해당 채팅방의 멤버인지 검증 (ChatInteractionService로 위임)
        chatInteractionService.verifyUserIsMemberOfRoom(senderId, roomId, chatRoomType);

        // 2. 검증 통과 후 메시지 생성 및 저장 (생성자 사용)
        ChatMessage message = new ChatMessage(roomId, senderId, content, messageType, chatRoomType);
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 3. 알림 이벤트
        List<ChatMember> roomMembers = chatRoomMemberRepository.findByChatRoomIdAndChatRoomTypeAndMember_IdNot(roomId, chatRoomType, senderId);

        for (ChatMember receiver : roomMembers) {
            if (receiver.isNotificationAlways()) {
                eventPublisher.publishEvent(
                        new NotificationEvent(
                                receiver.getMember().getId(),
                                senderId,
                                NotificationType.CHAT_MESSAGE,
                                savedMessage.getContent()
                        )
                );
            }
            // TODO : Mention 기능 지원하게 되면 Mention Only 알림 처리
        }
        // 4. 응답 생성
        return MessageResp.from(savedMessage, senderNickname);
    }

    @Transactional
    public MessageResp saveFileMessage(Long roomId, Long senderId, String senderNickname, String fileUrl, ChatMessage.MessageType messageType, ChatMessage.chatRoomType chatRoomType) {
        if (messageType != ChatMessage.MessageType.IMAGE && messageType != ChatMessage.MessageType.FILE) {
            throw new IllegalArgumentException("파일 메시지는 IMAGE 또는 FILE 타입이어야 합니다.");
        }
        return saveMessage(roomId, senderId, senderNickname, fileUrl, messageType, chatRoomType);
    }

    public List<MessageResp> getMessagesWithSenderInfo(Long roomId, ChatMessage.chatRoomType chatRoomType) {
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdAndChatRoomTypeOrderByCreatedAtAsc(roomId, chatRoomType);

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
