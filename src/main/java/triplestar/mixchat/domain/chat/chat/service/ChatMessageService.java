package triplestar.mixchat.domain.chat.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.repository.ChatMemberRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import triplestar.mixchat.domain.notification.constant.NotificationType;
import triplestar.mixchat.global.notifiaction.NotificationEvent;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomService chatRoomService; // 검증 로직을 위해 주입

    public MessageResp saveMessage(Long roomId, Long senderId, String senderNickname, String content, ChatMessage.MessageType messageType) {
        // 1. 사용자가 해당 채팅방의 멤버인지 검증 (캐시 -> DB 순서)
        chatRoomService.verifyUserIsMemberOfRoom(senderId, roomId);
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ApplicationEventPublisher eventPublisher;

        // 2. 검증 통과 후 메시지 생성 및 저장 (생성자 사용)
        ChatMessage message = new ChatMessage(roomId, senderId, content, messageType);
        ChatMessage savedMessage = chatMessageRepository.save(message);

        MessageResp resp = MessageResp.from(savedMessage, sender.getNickname());

        // 알림 이벤트
//        List<ChatMember> roomMembers = chatMemberRepository.findByChatRoomId(roomId, sender);
//
//        for (ChatMember receiver : roomMembers) {
//            if (receiver.isNotificationSettingAlways()) {
//                eventPublisher.publishEvent(
//                        new NotificationEvent(
//                                receiver.getMember().getId(),
//                                senderId,
//                                NotificationType.CHAT_MESSAGE,
//                                savedMessage.getContent()
//                        )
//                );
//            }
//            // TODO : Mention Only 알림 처리
//        }
        return resp;
        // 3. 응답 생성
        return MessageResp.from(savedMessage, senderNickname);
    }

    public MessageResp saveFileMessage(Long roomId, Long senderId, String senderNickname, String fileUrl, ChatMessage.MessageType messageType) {
        if (messageType != ChatMessage.MessageType.IMAGE && messageType != ChatMessage.MessageType.FILE) {
            throw new IllegalArgumentException("파일 메시지는 IMAGE 또는 FILE 타입이어야 합니다.");
        }
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
