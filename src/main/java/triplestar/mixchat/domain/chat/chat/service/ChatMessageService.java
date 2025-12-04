package triplestar.mixchat.domain.chat.chat.service;

import static java.time.LocalDateTime.now;
import static java.util.Collections.reverse;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.ai.chatbot.AiChatBotService;
import triplestar.mixchat.domain.ai.systemprompt.dto.TranslationReq;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.MessagePageResp;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.dto.MessageUnreadCountResp;
import triplestar.mixchat.domain.chat.chat.dto.RoomLastMessageUpdateResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage.MessageType;
import triplestar.mixchat.domain.chat.chat.repository.AIChatRoomRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.chat.repository.DirectChatRoomRepository;
import triplestar.mixchat.domain.chat.chat.repository.GroupChatRoomRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.notification.constant.NotificationType;
import triplestar.mixchat.global.ai.BotConstant;
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
    private final ChatNotificationService chatNotificationService;
    private final jakarta.persistence.EntityManager entityManager;
    private final AIChatRoomRepository aIChatRoomRepository;
    private final AiChatBotService aiChatBotService;

    @Transactional
    public MessageResp saveMessage(Long roomId, Long senderId, String senderNickname, String content, ChatMessage.MessageType messageType, ChatRoomType chatRoomType, boolean isTranslateEnabled) {
        // 시스템 메시지가 아닐 경우에만 멤버 검증을 수행
        if (messageType != ChatMessage.MessageType.SYSTEM) {
            chatMemberService.verifyUserIsMemberOfRoom(senderId, roomId, chatRoomType);

            // 1:1 채팅방인 경우 상대방이 존재하는지 확인 (차단/나가기 등으로 혼자 남은 경우 메시지 전송 불가)
            if (chatRoomType == ChatRoomType.DIRECT) {
                long memberCount = chatRoomMemberRepository.countByChatRoomIdAndChatRoomType(roomId, chatRoomType);
                if (memberCount < 2) {
                    throw new IllegalStateException("상대방이 채팅방을 나가 메시지를 보낼 수 없습니다.");
                }
            }
        }

        // Sequence 생성 (비관적 락으로 동시성 제어)
        Long sequence = generateSequence(roomId, chatRoomType);

        // 메시지 생성 및 저장
        ChatMessage message = new ChatMessage(roomId, senderId, sequence, content, messageType, chatRoomType, isTranslateEnabled);
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // AI 채팅방인 경우 별도로직 수행
        if (chatRoomType == ChatRoomType.AI) {
            MessageResp resp = MessageResp.from(savedMessage, senderNickname);
            chatNotificationService.sendChatMessage(roomId, chatRoomType, resp);

            // 사람이 보낸 메시지인 경우 ai 응답 생성 후 saveMessage 재귀 호출
            // 재귀 호출 후 봇이 보낸 메시지는 저장 및 전송만 수행
            if (!senderId.equals(BotConstant.BOT_MEMBER_ID)) {
                String chat = aiChatBotService.chat(senderId, roomId, content);
                saveMessage(roomId, BotConstant.BOT_MEMBER_ID, "Chat Bot", chat, MessageType.TEXT, chatRoomType, false);
            }
        }

        // TEXT 타입만 번역
        if (isTranslateEnabled && messageType == ChatMessage.MessageType.TEXT) {
            eventPublisher.publishEvent(new TranslationReq(savedMessage.getId(), savedMessage.getContent()));
        }

        // 1. 발신자 + 구독자 모두를 하나의 Set으로 수집 (읽음 처리 대상)
        Set<Long> memberIdsToMarkRead = new HashSet<>();
        memberIdsToMarkRead.add(senderId); // 발신자도 포함(방어적 코드)

        // 2. Redis에서 구독자 목록 조회 및 읽음 처리 대상에 추가
        Set<String> subscribers = subscriberCacheService.getSubscribers(roomId);
        if (subscribers != null && !subscribers.isEmpty()) {
            for (String subscriberIdStr : subscribers) {
                try {
                    Long subscriberId = Long.parseLong(subscriberIdStr);
                    memberIdsToMarkRead.add(subscriberId); // Set이므로 중복 자동 제거
                } catch (NumberFormatException e) {
                    log.error("구독자 ID 파싱 실패 - roomId: {}, 잘못된 ID: {}", roomId, subscriberIdStr);
                }
            }
        }

        // 3. Bulk Update: 발신자 + 구독자 모두 한 번에 처리 (쿼리 통합)
        if (!memberIdsToMarkRead.isEmpty()) {
            chatRoomMemberRepository.bulkUpdateLastReadSequence(
                roomId, chatRoomType, memberIdsToMarkRead, sequence, now()
            );
        }

        List<ChatMember> allMembers = chatRoomMemberRepository.findByChatRoomIdAndChatRoomType(roomId, chatRoomType);

        // 읽지 않은 사람 수
        int unreadCount = (int) allMembers.stream()
                .filter(member -> member.hasNotRead(sequence))
                .count();

        // 5. 알림 이벤트 (구독 중이지 않은 사람들에게만)
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

        // 6. 모든 멤버에게 채팅방 리스트 업데이트 알림 전송 (실시간 정렬용)
        String lastMessageAt = savedMessage.getCreatedAt().toString();
        String lastMessageContent = savedMessage.isTranslateEnabled() && savedMessage.getTranslatedContent() != null
                ? savedMessage.getTranslatedContent()
                : savedMessage.getContent();

        // 각 멤버별로 unreadCount를 계산해서 개별 전송
        allMembers.forEach(member -> {
            // 해당 멤버의 안읽은 메시지 수 계산 (현재 sequence - 마지막 읽은 sequence)
            long memberLastRead = member.getLastReadSequence();
            int unreadCountForMember = (int) Math.max(0, sequence - memberLastRead);

            RoomLastMessageUpdateResp updateResp = new RoomLastMessageUpdateResp(
                    roomId,
                    chatRoomType,
                    lastMessageAt,
                    unreadCountForMember,
                    lastMessageContent
            );

            chatNotificationService.sendRoomListUpdate(
                    member.getMember().getId().toString(),
                    updateResp
            );
        });

        MessageResp response = MessageResp.withUnreadCount(savedMessage, senderNickname, unreadCount);

        // 7. 현재 채팅방(Topic)에 메시지 전송
        chatNotificationService.sendChatMessage(roomId, chatRoomType, response);

        return response;
    }

    @Transactional
    public MessageResp saveFileMessage(Long roomId, Long senderId, String senderNickname, String fileUrl, ChatMessage.MessageType messageType, ChatRoomType chatRoomType) {
        if (messageType != ChatMessage.MessageType.IMAGE && messageType != ChatMessage.MessageType.FILE) {
            throw new IllegalArgumentException("파일 메시지는 IMAGE 또는 FILE 타입이어야 합니다.");
        }
        // 파일 메시지는 번역하지 않으므로 isTranslateEnabled는 항상 false
        return saveMessage(roomId, senderId, senderNickname, fileUrl, messageType, chatRoomType, false);
    }

    // 메시지 목록 조회 (페이징), 발신자 이름 및 unreadCount 포함
    public MessagePageResp getMessagesWithSenderInfo(Long roomId, ChatRoomType chatRoomType, Long requesterId, Long cursor, Integer size) {
        // 보안 검증: 요청자가 해당 채팅방의 멤버인지 확인
        chatMemberService.verifyUserIsMemberOfRoom(requesterId, roomId, chatRoomType);

        // 기본값: size = 25, 최대 100
        int pageSize = (size != null && size > 0 && size <= 100) ? size : 25;

        // 1. 채팅방의 모든 멤버 조회 (읽음 상태 확인 및 입장 시간 확인용)
        List<ChatMember> allMembers = chatRoomMemberRepository.findByChatRoomIdAndChatRoomType(roomId, chatRoomType);

        // 2. 요청자의 입장 시간 확인
        LocalDateTime joinDate = allMembers.stream()
                .filter(m -> m.getMember().getId().equals(requesterId))
                .map(ChatMember::getCreatedAt)
                .findFirst()
                .orElse(LocalDateTime.MIN);

        // 3. 메시지 조회 (sequence 내림차순 + 입장 시간 이후)
        List<ChatMessage> messages;
        if (cursor == null) {
            // 최신 메시지부터 pageSize개
            messages = chatMessageRepository.findByChatRoomIdAndChatRoomTypeAndCreatedAtGreaterThanEqualOrderBySequenceDesc(
                roomId, chatRoomType, joinDate, PageRequest.of(0, pageSize)
            );
        } else {
            // cursor 이전 메시지 pageSize개
            messages = chatMessageRepository.findByChatRoomIdAndChatRoomTypeAndSequenceLessThanAndCreatedAtGreaterThanEqualOrderBySequenceDesc(
                roomId, chatRoomType, cursor, joinDate, PageRequest.of(0, pageSize)
            );
        }

        // 역순 정렬 (오래된 메시지 → 최신 메시지 순으로 표시)
        reverse(messages);

        // 발신자 이름 조회
        List<Long> senderIds = messages.stream()
                .map(ChatMessage::getSenderId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> senderNames = memberRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));

        List<MessageResp> messageResps = messages.stream()
                .map(message -> {
                    String senderName = senderNames.getOrDefault(message.getSenderId(), "Unknown");

                    // 읽지 않은 사람 수
                    int unreadCount = (int) allMembers.stream()
                            .filter(member -> member.hasNotRead(message.getSequence()))
                            .count();

                    return MessageResp.withUnreadCount(message, senderName, unreadCount);
                })
                .collect(Collectors.toList());

        // 다음 페이지 정보 계산
        Long nextCursor = null;
        boolean hasMore = false;

        if (!messages.isEmpty()) {
            // nextCursor는 가장 오래된 메시지의 sequence (역순 정렬 후 첫 번째)
            nextCursor = messages.get(0).getSequence();
            // hasMore는 조회된 메시지 수가 pageSize와 같으면 true
            hasMore = messages.size() == pageSize;
        }

        return MessagePageResp.of(messageResps, nextCursor, hasMore);
    }

    // 누군가 채팅방 구독시 읽지 않은 사람 수 업데이트
    public List<MessageUnreadCountResp> getUnreadCountUpdates(Long roomId, ChatRoomType chatRoomType, Long readUpToSequence) {
        // 1. 채팅방의 모든 멤버 조회 (읽음 상태 확인용)
        List<ChatMember> allMembers = chatRoomMemberRepository.findByChatRoomIdAndChatRoomType(roomId, chatRoomType);

        // 최근 50개 업데이트(페이징이 25개이므로 여유있게 50개 조회)
        List<ChatMessage> recentMessages = chatMessageRepository.findByChatRoomIdAndChatRoomTypeOrderBySequenceDesc(
                roomId, chatRoomType, PageRequest.of(0, 50)
        );

        return recentMessages.stream()
                .filter(msg -> msg.getSequence() <= readUpToSequence) // readUpToSequence 이하만 필터링
                .map(message -> {
                    // 읽지 않은 사람 수
                    int unreadCount = (int) allMembers.stream()
                            .filter(member -> member.hasNotRead(message.getSequence()))
                            .count();

                    return new MessageUnreadCountResp(message.getId(), unreadCount);
                })
                .collect(Collectors.toList());
    }

    // Sequence 생성 (비관적 락으로 동시성 제어)
    private Long generateSequence(Long roomId, ChatRoomType chatRoomType) {
        // 1) room 개별 조회 및 시퀀스 생성
        Long newSeq = switch (chatRoomType) {
            case DIRECT -> directChatRoomRepository.findByIdWithLock(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId))
                    .generateNextSequence();

            case GROUP -> groupChatRoomRepository.findByIdWithLock(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId))
                    .generateNextSequence();

            // AI chat은 동시성 문제 없음
            case AI -> aIChatRoomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId))
                    .generateNextSequence();
        };

        // 2) 즉시 DB 반영
        entityManager.flush();
        return newSeq;
    }
}
