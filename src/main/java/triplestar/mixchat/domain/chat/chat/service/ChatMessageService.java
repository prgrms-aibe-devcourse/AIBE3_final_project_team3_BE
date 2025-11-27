package triplestar.mixchat.domain.chat.chat.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.dto.MessagePageResp;
import triplestar.mixchat.domain.chat.chat.dto.MessageResp;
import triplestar.mixchat.domain.chat.chat.dto.MessageUnreadCountDto;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
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
    private final jakarta.persistence.EntityManager entityManager;

    @Transactional
    public MessageResp saveMessage(Long roomId, Long senderId, String senderNickname, String content, ChatMessage.MessageType messageType, ChatMessage.chatRoomType chatRoomType) {
        // 멤버 검증
        chatMemberService.verifyUserIsMemberOfRoom(senderId, roomId, chatRoomType);

        // Sequence 생성 (비관적 락으로 동시성 제어)
        Long sequence = generateSequence(roomId, chatRoomType);

        // 메시지 생성 및 저장
        ChatMessage message = new ChatMessage(roomId, senderId, sequence, content, messageType, chatRoomType);
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 1. 발신자 + 구독자 모두를 하나의 Set으로 수집 (읽음 처리 대상)
        Set<Long> memberIdsToMarkRead = new HashSet<>();
        memberIdsToMarkRead.add(senderId); // 발신자도 포함

        // 2. Redis에서 구독자 목록 조회 및 수집
        Set<Long> subscribedMembers = new HashSet<>();
        Set<String> subscribers = subscriberCacheService.getSubscribers(roomId);
        log.info("[DEBUG Redis] roomId={}, subscribers from Redis={}", roomId, subscribers);
        if (subscribers != null && !subscribers.isEmpty()) {
            for (String subscriberIdStr : subscribers) {
                try {
                    Long subscriberId = Long.parseLong(subscriberIdStr);

                    // 발신자 제외한 구독자만 subscribedMembers에 추가 (unreadCount 계산용)
                    if (!subscriberId.equals(senderId)) {
                        subscribedMembers.add(subscriberId);
                    }

                    // 읽음 처리 대상에는 모두 추가 (Set이므로 중복 자동 제거)
                    memberIdsToMarkRead.add(subscriberId);
                } catch (NumberFormatException e) {
                    log.error("Redis에 잘못된 구독자 ID가 저장되어 있습니다: {}", subscriberIdStr);
                }
            }
        }
        log.info("[DEBUG memberIdsToMarkRead] memberIdsToMarkRead={}", memberIdsToMarkRead);

        // 3. Bulk Update: 발신자 + 구독자 모두 한 번에 처리 (쿼리 통합)
        if (!memberIdsToMarkRead.isEmpty()) {
            chatRoomMemberRepository.bulkUpdateLastReadSequence(
                roomId, chatRoomType, memberIdsToMarkRead, sequence, java.time.LocalDateTime.now()
            );
        }

        // 4. unreadCount 계산: lastReadSequence 기준으로 안 읽은 사람 수 계산
        // Bulk Update 후 다시 조회하여 업데이트된 lastReadSequence 사용
        List<ChatMember> allMembers = chatRoomMemberRepository.findByChatRoomIdAndChatRoomType(roomId, chatRoomType);

        log.info("[DEBUG unreadCount] roomId={}, sequence={}, senderId={}", roomId, sequence, senderId);
        for (ChatMember member : allMembers) {
            log.info("[DEBUG unreadCount] memberId={}, lastReadSequence={}",
                    member.getMember().getId(), member.getLastReadSequence());
        }

        int unreadCount = (int) allMembers.stream()
                .filter(member -> !member.getMember().getId().equals(senderId)) // 발신자 제외
                .filter(member -> member.getLastReadSequence() == null ||
                        member.getLastReadSequence() < sequence) // 안 읽은 사람
                .count();

        log.info("[DEBUG unreadCount] calculated unreadCount={}", unreadCount);

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

        return MessageResp.withUnreadCount(savedMessage, senderNickname, unreadCount);
    }

    @Transactional
    public MessageResp saveFileMessage(Long roomId, Long senderId, String senderNickname, String fileUrl, ChatMessage.MessageType messageType, ChatMessage.chatRoomType chatRoomType) {
        if (messageType != ChatMessage.MessageType.IMAGE && messageType != ChatMessage.MessageType.FILE) {
            throw new IllegalArgumentException("파일 메시지는 IMAGE 또는 FILE 타입이어야 합니다.");
        }
        return saveMessage(roomId, senderId, senderNickname, fileUrl, messageType, chatRoomType);
    }

    public MessagePageResp getMessagesWithSenderInfo(Long roomId, ChatMessage.chatRoomType chatRoomType, Long requesterId, Long cursor, Integer size) {
        // 기본값: size = 25, 최대 100
        int pageSize = (size != null && size > 0 && size <= 100) ? size : 25;

        log.info("[getMessagesWithSenderInfo] roomId={}, chatRoomType={}, cursor={}, size={}", roomId, chatRoomType, cursor, pageSize);

        // 메시지 조회 (sequence 내림차순)
        List<ChatMessage> messages;
        if (cursor == null) {
            // 최신 메시지부터 pageSize개
            messages = chatMessageRepository.findByChatRoomIdAndChatRoomTypeOrderBySequenceDesc(
                roomId, chatRoomType, org.springframework.data.domain.PageRequest.of(0, pageSize)
            );
        } else {
            // cursor 이전 메시지 pageSize개
            messages = chatMessageRepository.findByChatRoomIdAndChatRoomTypeAndSequenceLessThanOrderBySequenceDesc(
                roomId, chatRoomType, cursor, org.springframework.data.domain.PageRequest.of(0, pageSize)
            );
        }

        // 역순 정렬 (오래된 메시지 → 최신 메시지 순으로 표시)
        java.util.Collections.reverse(messages);

        log.info("[getMessagesWithSenderInfo] Found {} messages for roomId={}", messages.size(), roomId);

        // 발신자 이름 조회
        List<Long> senderIds = messages.stream()
                .map(ChatMessage::getSenderId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> senderNames = memberRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getNickname));

        // 채팅방의 모든 멤버 조회 (읽음 상태 확인용)
        List<ChatMember> allMembers = chatRoomMemberRepository.findByChatRoomIdAndChatRoomType(roomId, chatRoomType);

        List<MessageResp> messageResps = messages.stream()
                .map(message -> {
                    String senderName = senderNames.getOrDefault(message.getSenderId(), "Unknown");

                    // unreadCount 계산: 발신자를 제외한 멤버 중 안 읽은 사람 수
                    int unreadCount = (int) allMembers.stream()
                            .filter(member -> !member.getMember().getId().equals(message.getSenderId()))
                            .filter(member -> member.getLastReadSequence() == null ||
                                            member.getLastReadSequence() < message.getSequence())
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

    public List<MessageUnreadCountDto> getUnreadCountUpdates(Long roomId, ChatMessage.chatRoomType chatRoomType, Long readUpToSequence) {
        // 1. 채팅방의 모든 멤버 조회 (읽음 상태 확인용)
        List<ChatMember> allMembers = chatRoomMemberRepository.findByChatRoomIdAndChatRoomType(roomId, chatRoomType);

        // 2. 성능 최적화: 최근 200개 메시지만 조회
        // 이유:
        // - 화면에 보이는 건 보통 25개
        // - 여러 사용자가 스크롤업으로 100개 정도까지 볼 수 있음
        // - 200개면 대부분의 실시간 케이스 커버
        // - 그보다 오래된 메시지는 페이지 로드 시 서버가 정확히 계산하므로 문제없음
        List<ChatMessage> recentMessages = chatMessageRepository.findByChatRoomIdAndChatRoomTypeOrderBySequenceDesc(
                roomId, chatRoomType, org.springframework.data.domain.PageRequest.of(0, 200)
        );

        // 3. readUpToSequence 이하만 필터링
        List<ChatMessage> affectedMessages = recentMessages.stream()
                .filter(msg -> msg.getSequence() <= readUpToSequence)
                .collect(Collectors.toList());

        log.info("[getUnreadCountUpdates] Found {} affected messages (out of {} recent) for roomId={} up to sequence={}",
                affectedMessages.size(), recentMessages.size(), roomId, readUpToSequence);

        // 3. 각 메시지의 unreadCount를 다시 계산하여 DTO 리스트 생성
        log.info("[getUnreadCountUpdates] Calculating unreadCount for {} messages. Total members in room: {}",
                affectedMessages.size(), allMembers.size());

        // 디버깅: 멤버 정보 출력
        allMembers.forEach(member ->
            log.debug("[Member] id={}, lastReadSequence={}",
                member.getMember().getId(), member.getLastReadSequence())
        );

        return affectedMessages.stream()
                .map(message -> {
                    // unreadCount 계산: 발신자를 제외한 멤버 중 안 읽은 사람 수
                    int unreadCount = (int) allMembers.stream()
                            .filter(member -> !member.getMember().getId().equals(message.getSenderId()))
                            .filter(member -> member.getLastReadSequence() == null ||
                                    member.getLastReadSequence() < message.getSequence())
                            .count();

                    log.debug("[unreadCount] msgId={}, sequence={}, senderId={}, unreadCount={}",
                            message.getId(), message.getSequence(), message.getSenderId(), unreadCount);

                    return new MessageUnreadCountDto(message.getId(), unreadCount);
                })
                .collect(Collectors.toList());
    }

    // Sequence 생성 (비관적 락으로 동시성 제어)
    private Long generateSequence(Long roomId, ChatMessage.chatRoomType chatRoomType) {
        Long sequence = switch (chatRoomType) {
            case DIRECT -> {
                var room = directChatRoomRepository.findByIdWithLock(roomId)
                        .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId));
                Long beforeSeq = room.getCurrentSequence();
                Long newSeq = room.generateNextSequence();
                entityManager.flush(); // 즉시 DB에 반영
                log.info("[DEBUG Sequence] DIRECT roomId={}, before={}, after={}, flushed", roomId, beforeSeq, newSeq);
                yield newSeq;
            }
            case GROUP -> {
                var room = groupChatRoomRepository.findByIdWithLock(roomId)
                        .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다. ID: " + roomId));
                Long beforeSeq = room.getCurrentSequence();
                Long newSeq = room.generateNextSequence();
                entityManager.flush(); // 즉시 DB에 반영
                log.info("[DEBUG Sequence] GROUP roomId={}, before={}, after={}, flushed", roomId, beforeSeq, newSeq);
                yield newSeq;
            }
            default -> throw new UnsupportedOperationException("지원하지 않는 채팅방 타입입니다: " + chatRoomType);
        };
        return sequence;
    }
}
