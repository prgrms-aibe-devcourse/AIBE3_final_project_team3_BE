package triplestar.mixchat.domain.chat.chat.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
import triplestar.mixchat.domain.chat.chat.constant.ChatNotificationSetting;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.MessagePageResp;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.dto.MessageUnreadCountResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage.MessageType;
import triplestar.mixchat.domain.chat.chat.event.ChatMessageCreatedEvent;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.search.service.ChatMessageSearchService;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.notification.constant.NotificationType;
import triplestar.mixchat.global.ai.BotMemberProvider;
import triplestar.mixchat.global.cache.ChatSubscriberCacheService;
import triplestar.mixchat.global.notifiaction.NotificationEvent;

import triplestar.mixchat.domain.member.presence.service.PresenceService;

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
    private final ChatNotificationService chatNotificationService;
    private final AiChatBotService aiChatBotService;
    private final ChatSequenceGenerator sequenceGenerator;
    private final ChatMessageSearchService chatMessageSearchService;
    private final ChatSubscriberCacheService subscriberCacheService;
    private final PresenceService presenceService;
    private final BotMemberProvider botMemberProvider;

    @Transactional
    public MessageResp saveMessage(Long roomId, Long senderId, String senderNickname, String content,
                                   ChatMessage.MessageType messageType, ChatRoomType chatRoomType,
                                   boolean isTranslateEnabled) {
        String resolvedNickname = resolveSenderNickname(senderId, senderNickname);
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

        // Sequence 생성 (Redis INCR 기반)
        Long sequence = sequenceGenerator.generateSequence(roomId, chatRoomType);

        // 메시지 생성 및 저장
        ChatMessage message = new ChatMessage(roomId, senderId, sequence, content, messageType, chatRoomType,
                isTranslateEnabled);
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // AI 채팅방인 경우 메시지 생성 및 저장 후 별도 로직 수행
        if (chatRoomType == ChatRoomType.AI) {
            return saveAiRoomMessage(roomId, senderId, resolvedNickname, content, chatRoomType, savedMessage);
        }

        // === 트랜잭션 내에서 모든 DB 연산 처리 (방향 A 패턴) ===

        // 1. 읽음 상태 업데이트 (발신자 + 구독자 중 온라인인 사람)
        Set<Long> memberIdsToMarkRead = new HashSet<>();
        memberIdsToMarkRead.add(senderId);

        // 현재 구독 중인 사용자 조회 (Redis)
        Set<String> subscribers = subscriberCacheService.getSubscribers(roomId);
        List<Long> subscriberIds = new ArrayList<>();
        if (subscribers != null && !subscribers.isEmpty()) {
            for (String subscriberIdStr : subscribers) {
                try {
                    subscriberIds.add(Long.parseLong(subscriberIdStr));
                } catch (NumberFormatException e) {
                    log.error("구독자 ID 파싱 실패 - roomId: {}, 잘못된 ID: {}", roomId, subscriberIdStr);
                }
            }
        }

        // 실제로 온라인인 사람만 필터링
        if (!subscriberIds.isEmpty()) {
            Set<Long> onlineSubscribers = presenceService.filterIsOnline(subscriberIds);
            memberIdsToMarkRead.addAll(onlineSubscribers);
        }

        if (!memberIdsToMarkRead.isEmpty()) {
            chatRoomMemberRepository.bulkUpdateLastReadSequence(
                    roomId, chatRoomType, memberIdsToMarkRead, sequence, LocalDateTime.now()
            );
        }

        // 2. unreadCount 계산
       List<ChatRoomMemberRepository.MemberSummary> memberSummaries =
                chatRoomMemberRepository.findMemberSummariesByRoomIdAndChatRoomType(roomId, chatRoomType);

        int unreadCount = (int) memberSummaries.stream()
                .filter(summary -> {
                    Long lastRead = summary.getLastReadSequence();
                    return lastRead == null || lastRead < sequence;
                })
                .count();

        // 3. 알림 이벤트 생성 (DB 저장 또는 발행용 데이터 준비)
        List<NotificationEvent> notificationEvents = new ArrayList<>();
        for (ChatRoomMemberRepository.MemberSummary receiver : memberSummaries) {
            Long receiverId = receiver.getMemberId();
            if (receiverId.equals(senderId) || subscriberIds.contains(receiverId)) {
                continue;
            }
            if (receiver.getChatNotificationSetting() == ChatNotificationSetting.ALWAYS) {
                notificationEvents.add(
                        new NotificationEvent(
                                receiverId,
                                senderId,
                                NotificationType.CHAT_MESSAGE,
                                content
                        )
                );
            }
        }

        // 4. 이벤트 발행 (AFTER_COMMIT에서 외부 I/O만 처리)
        ChatMessageCreatedEvent createdEvent = new ChatMessageCreatedEvent(
                savedMessage.getId(),
                savedMessage.getChatRoomId(),
                savedMessage.getSenderId(),
                resolvedNickname,
                savedMessage.getContent(),
                savedMessage.getMessageType(),
                savedMessage.getChatRoomType(),
                savedMessage.getSequence(),
                savedMessage.isTranslateEnabled(),
                savedMessage.getCreatedAt(),
                unreadCount
        );
        eventPublisher.publishEvent(createdEvent);

        // 알림 이벤트 발행 (개별 발행)
        notificationEvents.forEach(eventPublisher::publishEvent);

        // 최소 정보 + unreadCount 반환
        return MessageResp.withUnreadCount(savedMessage, resolvedNickname, unreadCount);
    }

    private MessageResp saveAiRoomMessage(Long roomId, Long senderId, String senderNickname, String content,
                                          ChatRoomType chatRoomType, ChatMessage savedMessage) {
        // 웹소켓 알림 전송
        MessageResp resp = MessageResp.from(savedMessage, senderNickname);
        chatNotificationService.sendChatMessage(roomId, chatRoomType, resp);

        // 사람이 보낸 메시지인 경우 ai 응답 생성을 위해 aiChatBotService.chat() 이후 saveMessage 재귀 호출
        if (!senderId.equals(botMemberProvider.getBotMemberId())) {
            String chat = aiChatBotService.chat(senderId, roomId, content);
            return saveMessage(roomId, botMemberProvider.getBotMemberId(), "Chat Bot", chat, MessageType.TEXT,
                    chatRoomType, false);
        }

        // 재귀 호출 후에는 봇이 보낸 메시지이므로 저장 및 전송만 수행
        return resp;
    }

    private String resolveSenderNickname(Long senderId, String senderNickname) {
        if (senderNickname != null && !senderNickname.isBlank()) {
            return senderNickname;
        }

        return memberRepository.findById(senderId)
                .map(Member::getNickname)
                .orElseThrow(() -> new IllegalArgumentException("메시지 발신자 정보가 유효하지 않습니다."));
    }

    @Transactional
    public MessageResp saveFileMessage(Long roomId, Long senderId, String senderNickname, String fileUrl,
                                       ChatMessage.MessageType messageType, ChatRoomType chatRoomType) {
        if (messageType != ChatMessage.MessageType.IMAGE && messageType != ChatMessage.MessageType.FILE) {
            throw new IllegalArgumentException("파일 메시지는 IMAGE 또는 FILE 타입이어야 합니다.");
        }
        // 파일 메시지는 번역하지 않으므로 isTranslateEnabled는 항상 false
        return saveMessage(roomId, senderId, senderNickname, fileUrl, messageType, chatRoomType, false);
    }

    // 메시지 목록 조회 (페이징), 발신자 이름 및 unreadCount 포함
    public MessagePageResp getMessagesWithSenderInfo(Long roomId, ChatRoomType chatRoomType, Long requesterId,
                                                     Long cursor, Integer size) {
        // 보안 검증: 요청자가 해당 채팅방의 멤버인지 확인
        chatMemberService.verifyUserIsMemberOfRoom(requesterId, roomId, chatRoomType);

        // 기본값: size = 25, 최대 100
        int pageSize = (size != null && size > 0 && size <= 100) ? size : 25;

        // AI 채팅방은 별도처리
        if (chatRoomType.equals(ChatRoomType.AI)) {
            return getMessagePageResp(roomId, chatRoomType, pageSize);
        }

        // 1. 채팅방의 모든 멤버 조회 (읽음 상태 확인 및 입장 시간 확인용)
        List<ChatMember> allMembers = chatRoomMemberRepository.findByChatRoomIdAndChatRoomType(roomId, chatRoomType);
        List<Long> sortedLastReadSequences = allMembers.stream()
                .map(cm -> cm.getLastReadSequence() == null ? 0L : cm.getLastReadSequence())
                .sorted()
                .toList();

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

        // 발신자 이름 조회
        List<Long> senderIds = messages.stream()
                .map(ChatMessage::getSenderId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> senderNames = memberRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));

        // Two Pointer 알고리즘 적용 (O(N+M))
        int[] unreadCounts = new int[messages.size()];
        int memberIdx = 0;
        int memberCount = sortedLastReadSequences.size();

        // 메시지는 내림차순(최신->과거)이므로, 역순(과거->최신)으로 순회하며 비교
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage message = messages.get(i);

            // 현재 메시지 시퀀스보다 lastReadSequence가 작은 멤버 수 카운트 (포인터 전진)
            while (memberIdx < memberCount && sortedLastReadSequences.get(memberIdx) < message.getSequence()) {
                memberIdx++;
            }
            unreadCounts[i] = memberIdx;
        }

        List<MessageResp> messageResps = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage message = messages.get(i);
            String senderName = senderNames.getOrDefault(message.getSenderId(), "Unknown");
            messageResps.add(MessageResp.withUnreadCount(message, senderName, unreadCounts[i]));
        }

        // 다음 페이지 정보 계산
        Long nextCursor = null;
        boolean hasMore = false;

        if (!messages.isEmpty()) {
            // 리스트는 sequence desc 정렬이므로 마지막 요소가 가장 오래된 메시지
            nextCursor = messages.get(messages.size() - 1).getSequence();
            // hasMore는 조회된 메시지 수가 pageSize와 같으면 true
            hasMore = messages.size() == pageSize;
        }

        return MessagePageResp.of(messageResps, nextCursor, hasMore);
    }

    // AI 채팅방 전용 메시지 조회 (입장 시간 필터 없음)
    private MessagePageResp getMessagePageResp(Long roomId, ChatRoomType chatRoomType, int pageSize) {
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdAndChatRoomTypeAndCreatedAtGreaterThanEqualOrderBySequenceDesc(
                roomId, chatRoomType, LocalDateTime.of(2000, 1, 1, 0, 0), PageRequest.of(0, pageSize)
        );

        List<MessageResp> messageResps = messages.stream()
                .map(message -> MessageResp.from(message, "user"))
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

    public void broadcastReadStatus(Long roomId, ChatRoomType chatRoomType, Long readUpToSequence) {
        if (chatRoomType == ChatRoomType.AI) return;

        // 변경된 unreadCount 계산
        List<MessageUnreadCountResp> updates = getUnreadCountUpdates(roomId, chatRoomType, readUpToSequence);

        // 알림 전송
        chatNotificationService.sendUnreadCountUpdate(roomId, chatRoomType, updates);
    }

    // 누군가 채팅방 구독시 읽지 않은 사람 수 업데이트
    public List<MessageUnreadCountResp> getUnreadCountUpdates(Long roomId, ChatRoomType chatRoomType,
                                                              Long readUpToSequence) {
        // 1. 채팅방의 모든 멤버 조회 (읽음 상태 확인용)
        List<ChatMember> allMembers = chatRoomMemberRepository.findByChatRoomIdAndChatRoomType(roomId, chatRoomType);

        // 멤버들의 lastReadSequence 미리 추출 및 오름차순 정렬 (O(N log N))
        List<Long> sortedLastReadSequences = allMembers.stream()
                .map(cm -> cm.getLastReadSequence() == null ? 0L : cm.getLastReadSequence())
                .sorted()
                .toList();

        // 최근 50개 업데이트(페이징이 25개이므로 여유있게 50개 조회) - 시퀀스 내림차순(최신순)
        List<ChatMessage> recentMessages = chatMessageRepository.findByChatRoomIdAndChatRoomTypeOrderBySequenceDesc(
                roomId, chatRoomType, PageRequest.of(0, 50)
        );

        List<ChatMessage> targetMessages = recentMessages.stream()
                .filter(msg -> msg.getSequence() <= readUpToSequence) // readUpToSequence 이하만 필터링
                .collect(Collectors.toList());

        // 투 포인터 알고리즘 적용 (O(N+M))
        int[] unreadCounts = new int[targetMessages.size()];
        int memberIdx = 0;
        int memberCount = sortedLastReadSequences.size();

        // 메시지는 내림차순이므로, 역순(과거순)으로 순회하여 오름차순인 sortedLastReadSequences와 비교
        for (int i = targetMessages.size() - 1; i >= 0; i--) {
            ChatMessage message = targetMessages.get(i);

            // 현재 메시지 시퀀스보다 lastReadSequence가 작은 멤버 수 카운트 (포인터 전진)
            while (memberIdx < memberCount && sortedLastReadSequences.get(memberIdx) < message.getSequence()) {
                memberIdx++;
            }
            unreadCounts[i] = memberIdx;
        }

        List<MessageUnreadCountResp> result = new ArrayList<>();
        for (int i = 0; i < targetMessages.size(); i++) {
            result.add(new MessageUnreadCountResp(targetMessages.get(i).getId(), unreadCounts[i]));
        }

        return result;
    }
}
