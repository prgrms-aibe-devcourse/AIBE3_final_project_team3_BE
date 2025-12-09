package triplestar.mixchat.domain.chat.chat.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.LoadTestCleanupReq;
import triplestar.mixchat.domain.chat.chat.dto.LoadTestCleanupResp;
import triplestar.mixchat.domain.chat.chat.entity.AIChatRoom;
import triplestar.mixchat.domain.chat.chat.entity.DirectChatRoom;
import triplestar.mixchat.domain.chat.chat.entity.GroupChatRoom;
import triplestar.mixchat.domain.chat.chat.repository.AIChatRoomRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.chat.repository.DirectChatRoomRepository;
import triplestar.mixchat.domain.chat.chat.repository.GroupChatRoomRepository;
import triplestar.mixchat.global.cache.ChatAuthCacheService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LoadTestCleanupService {

    private final DirectChatRoomRepository directChatRoomRepository;
    private final GroupChatRoomRepository groupChatRoomRepository;
    private final AIChatRoomRepository aiChatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatAuthCacheService chatAuthCacheService;

    @Transactional
    public LoadTestCleanupResp cleanupLoadTestData(LoadTestCleanupReq request) {
        long startTime = System.currentTimeMillis();

        log.info("부하테스트 데이터 정리 시작 - testAccountIds: {}, createdAfter: {}, dryRun: {}",
                request.testAccountIds(), request.createdAfter(), request.isDryRun());

        int deletedDirectRooms = 0;
        int deletedGroupRooms = 0;
        int deletedAIRooms = 0;
        int deletedMembers = 0;
        long deletedMessages = 0;

        // 1. Direct 채팅방 정리 (테스트 계정 간의 채팅방)
        List<DirectChatRoom> directRooms = directChatRoomRepository.findByTestAccounts(request.testAccountIds());
        directRooms = filterByCreatedTime(directRooms, request.createdAfter());

        log.info("Direct 채팅방 {} 개 발견", directRooms.size());
        for (DirectChatRoom room : directRooms) {
            deletedMessages += cleanupRoomData(room.getId(), ChatRoomType.DIRECT, request.isDryRun());
            deletedMembers += cleanupRoomMembers(room.getId(), ChatRoomType.DIRECT, request.isDryRun());

            if (!request.isDryRun()) {
                chatAuthCacheService.removeRoom(room.getId());
                directChatRoomRepository.deleteById(room.getId());
            }
            deletedDirectRooms++;
        }

        // 2. Group 채팅방 정리 ([LOAD_TEST] 태그)
        List<GroupChatRoom> groupRooms = groupChatRoomRepository.findLoadTestRooms();
        groupRooms = filterByCreatedTime(groupRooms, request.createdAfter());

        log.info("Group 채팅방 {} 개 발견", groupRooms.size());
        for (GroupChatRoom room : groupRooms) {
            deletedMessages += cleanupRoomData(room.getId(), ChatRoomType.GROUP, request.isDryRun());
            deletedMembers += cleanupRoomMembers(room.getId(), ChatRoomType.GROUP, request.isDryRun());

            if (!request.isDryRun()) {
                chatAuthCacheService.removeRoom(room.getId());
                groupChatRoomRepository.deleteById(room.getId());
            }
            deletedGroupRooms++;
        }

        // 3. AI 채팅방 정리 ([LOAD_TEST] 태그)
        List<AIChatRoom> aiRooms = aiChatRoomRepository.findLoadTestRooms();
        aiRooms = filterByCreatedTime(aiRooms, request.createdAfter());

        log.info("AI 채팅방 {} 개 발견", aiRooms.size());
        for (AIChatRoom room : aiRooms) {
            deletedMessages += cleanupRoomData(room.getId(), ChatRoomType.AI, request.isDryRun());
            deletedMembers += cleanupRoomMembers(room.getId(), ChatRoomType.AI, request.isDryRun());

            if (!request.isDryRun()) {
                chatAuthCacheService.removeRoom(room.getId());
                aiChatRoomRepository.deleteById(room.getId());
            }
            deletedAIRooms++;
        }

        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("부하테스트 데이터 정리 완료 - Direct: {}, Group: {}, AI: {}, Members: {}, Messages: {}, 소요시간: {}ms, DryRun: {}",
                deletedDirectRooms, deletedGroupRooms, deletedAIRooms, deletedMembers, deletedMessages, elapsedTime, request.isDryRun());

        return LoadTestCleanupResp.of(
                deletedDirectRooms,
                deletedGroupRooms,
                deletedAIRooms,
                deletedMembers,
                deletedMessages,
                request.isDryRun(),
                elapsedTime
        );
    }

    /**
     * 채팅방의 메시지 데이터 삭제 (MongoDB)
     * @return 삭제된 메시지 수
     */
    private long cleanupRoomData(Long roomId, ChatRoomType roomType, boolean dryRun) {
        long messageCount = chatMessageRepository.countByChatRoomIdAndChatRoomType(roomId, roomType);

        if (!dryRun && messageCount > 0) {
            chatMessageRepository.deleteByChatRoomIdAndChatRoomType(roomId, roomType);
            log.debug("채팅방 메시지 삭제 - roomId: {}, roomType: {}, count: {}", roomId, roomType, messageCount);
        }

        return messageCount;
    }

    /**
     * 채팅방의 멤버 데이터 삭제 (MySQL)
     * @return 삭제된 멤버 수
     */
    private int cleanupRoomMembers(Long roomId, ChatRoomType roomType, boolean dryRun) {
        int memberCount = chatRoomMemberRepository.findByChatRoomIdAndChatRoomType(roomId, roomType).size();

        if (!dryRun && memberCount > 0) {
            chatRoomMemberRepository.deleteByChatRoomIdAndChatRoomType(roomId, roomType);
            log.debug("채팅방 멤버 삭제 - roomId: {}, roomType: {}, count: {}", roomId, roomType, memberCount);
        }

        return memberCount;
    }

    /**
     * 생성 시간 필터링 (선택적)
     */
    private <T> List<T> filterByCreatedTime(List<T> rooms, LocalDateTime createdAfter) {
        if (createdAfter == null) {
            return rooms;
        }

        List<T> filtered = new ArrayList<>();
        for (T room : rooms) {
            LocalDateTime createdAt = getCreatedAt(room);
            if (createdAt != null && createdAt.isAfter(createdAfter)) {
                filtered.add(room);
            }
        }

        log.debug("시간 필터링 - 전체: {}, 필터 후: {}", rooms.size(), filtered.size());
        return filtered;
    }

    /**
     * Entity의 생성 시간 추출 (BaseEntity의 createdAt 필드 사용)
     */
    private LocalDateTime getCreatedAt(Object entity) {
        if (entity instanceof DirectChatRoom) {
            return ((DirectChatRoom) entity).getCreatedAt();
        } else if (entity instanceof GroupChatRoom) {
            return ((GroupChatRoom) entity).getCreatedAt();
        } else if (entity instanceof AIChatRoom) {
            return ((AIChatRoom) entity).getCreatedAt();
        }
        return null;
    }
}
