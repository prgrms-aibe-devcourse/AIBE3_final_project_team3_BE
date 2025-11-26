package triplestar.mixchat.domain.chat.chat.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.entity.DirectChatRoom;
import triplestar.mixchat.domain.chat.chat.entity.GroupChatRoom;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.chat.repository.DirectChatRoomRepository;
import triplestar.mixchat.domain.chat.chat.repository.GroupChatRoomRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.notification.constant.NotificationType;
import triplestar.mixchat.global.cache.ChatSubscriberCacheService;
import triplestar.mixchat.global.notifiaction.NotificationEvent;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatMemberService chatMemberService;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final DirectChatRoomRepository directChatRoomRepository;
    private final GroupChatRoomRepository groupChatRoomRepository;
    private final ChatSubscriberCacheService subscriberCacheService;

    @Transactional
    public MessageResp saveMessage(Long roomId, Long senderId, String senderNickname, String content, ChatMessage.MessageType messageType, ChatMessage.chatRoomType chatRoomType) {
        // 멤버 검증
        chatMemberService.verifyUserIsMemberOfRoom(senderId, roomId, chatRoomType);

        // Sequence 생성 (비관적 락으로 동시성 제어)
        Long sequence = generateSequence(roomId, chatRoomType);

        // 메시지 생성 및 저장
        ChatMessage message = new ChatMessage(roomId, senderId, sequence, content, messageType, chatRoomType);
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // AI 채팅방은 unreadCount 및 구독자 관리 불필요
        if (chatRoomType == ChatMessage.chatRoomType.AI) {
            return MessageResp.from(savedMessage, senderNickname, 0);
        }

        // 1. 발신자의 lastReadSequence 자동 업데이트
        chatMemberService.updateLastReadSequence(senderId, roomId, chatRoomType, sequence);

        // 2. 현재 채팅방에 구독 중인 사람들 자동 읽음 처리 (Bulk Update로 성능 최적화)
        Set<Long> subscribedMembers = new java.util.HashSet<>();
        Set<String> subscribers = subscriberCacheService.getSubscribers(roomId);
        if (subscribers != null && !subscribers.isEmpty()) {
            Set<Long> subscriberIds = new java.util.HashSet<>();
            for (String subscriberIdStr : subscribers) {
                try {
                    Long subscriberId = Long.parseLong(subscriberIdStr);
                    if (!subscriberId.equals(senderId)) {
                        subscriberIds.add(subscriberId);
                    }
                } catch (NumberFormatException e) {
                    log.error("Invalid subscriber ID in Redis: {}", subscriberIdStr);
                }
            }

            // Bulk Update: n명의 개별 UPDATE 쿼리 대신 1개의 UPDATE 쿼리로 처리
            if (!subscriberIds.isEmpty()) {
                chatRoomMemberRepository.bulkUpdateLastReadSequence(
                    roomId, chatRoomType, subscriberIds, sequence, java.time.LocalDateTime.now()
                );
                subscribedMembers.addAll(subscriberIds);
            }
        }

        // 3. unreadCount 계산: 전체 멤버 수 - 1(발신자) - 구독 중인 사람 수
        List<ChatMember> allMembers = chatRoomMemberRepository.findByChatRoomIdAndChatRoomType(roomId, chatRoomType);
        int unreadCount = allMembers.size() - 1 - subscribedMembers.size();

        // 4. 알림 이벤트 (구독 중이지 않은 사람들에게만)
        List<ChatMember> roomMembers = chatRoomMemberRepository.findByChatRoomIdAndChatRoomTypeAndMember_IdNot(roomId, chatRoomType, senderId);
        for (ChatMember receiver : roomMembers) {
            if (subscriberCacheService.isSubscribed(roomId, receiver.getMember().getId())) {
                continue;
            }
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
        }

        return MessageResp.from(savedMessage, senderNickname, unreadCount);
    }

    @Transactional
    public MessageResp saveFileMessage(Long roomId, Long senderId, String senderNickname, String fileUrl, ChatMessage.MessageType messageType, ChatMessage.chatRoomType chatRoomType) {
        if (messageType != ChatMessage.MessageType.IMAGE && messageType != ChatMessage.MessageType.FILE) {
            throw new IllegalArgumentException("파일 메시지는 IMAGE 또는 FILE 타입이어야 합니다.");
        }
        return saveMessage(roomId, senderId, senderNickname, fileUrl, messageType, chatRoomType);
    }

    public List<MessageResp> getMessagesWithSenderInfo(Long roomId, ChatMessage.chatRoomType chatRoomType, Long requesterId) {
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdAndChatRoomTypeOrderByCreatedAtAsc(roomId, chatRoomType);

        // 발신자 이름 조회
        List<Long> senderIds = messages.stream()
                .map(ChatMessage::getSenderId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> senderNames = memberRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));

        // AI 채팅방은 unreadCount 계산 불필요
        if (chatRoomType == ChatMessage.chatRoomType.AI) {
            return messages.stream()
                    .map(message -> {
                        String senderName = senderNames.getOrDefault(message.getSenderId(), "Unknown");
                        return MessageResp.from(message, senderName, 0);
                    })
                    .collect(Collectors.toList());
        }

        // 채팅방의 모든 멤버 조회 (읽음 상태 확인용)
        // todo: 부하 심해지면 프론트에서 연산 고려? 하지만 신뢰성이 낮을듯
        List<ChatMember> allMembers = chatRoomMemberRepository.findByChatRoomIdAndChatRoomType(roomId, chatRoomType);

        return messages.stream()
                .map(message -> {
                    String senderName = senderNames.getOrDefault(message.getSenderId(), "Unknown");

                    // unreadCount 계산: 발신자를 제외한 멤버 중 안 읽은 사람 수
                    int unreadCount = (int) allMembers.stream()
                            .filter(member -> !member.getMember().getId().equals(message.getSenderId()))
                            .filter(member -> member.getLastReadSequence() == null ||
                                            member.getLastReadSequence() < message.getSequence())
                            .count();

                    return MessageResp.from(message, senderName, unreadCount);
                })
                .collect(Collectors.toList());
    }

    // Sequence 생성 (비관적 락으로 동시성 제어)
    private Long generateSequence(Long roomId, ChatMessage.chatRoomType chatRoomType) {
        if (chatRoomType == ChatMessage.chatRoomType.DIRECT) {
            DirectChatRoom room = directChatRoomRepository.findByIdWithLock(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
            Long seq = room.generateNextSequence();
            directChatRoomRepository.save(room);
            return seq;
        } else if (chatRoomType == ChatMessage.chatRoomType.GROUP) {
            GroupChatRoom room = groupChatRoomRepository.findByIdWithLock(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
            Long seq = room.generateNextSequence();
            groupChatRoomRepository.save(room);
            return seq;
        }
        throw new UnsupportedOperationException("지원하지 않는 채팅방 타입입니다.");
    }
}
