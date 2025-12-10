package triplestar.mixchat.domain.chat.chat.service;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
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
@Profile({"dev", "local", "test"})  // 개발/로컬/테스트 환경에서만 활성화, 프로덕션에서는 비활성화
public class LoadTestCleanupService {

    private final DirectChatRoomRepository directChatRoomRepository;
    private final GroupChatRoomRepository groupChatRoomRepository;
    private final AIChatRoomRepository aiChatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatAuthCacheService chatAuthCacheService;

    // 테스트 계정 ID 하드코딩 (1~100번)
    private static final List<Long> TEST_ACCOUNT_IDS = Arrays.asList(
            1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L,
            11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L,
            21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L, 30L,
            31L, 32L, 33L, 34L, 35L, 36L, 37L, 38L, 39L, 40L,
            41L, 42L, 43L, 44L, 45L, 46L, 47L, 48L, 49L, 50L,
            51L, 52L, 53L, 54L, 55L, 56L, 57L, 58L, 59L, 60L,
            61L, 62L, 63L, 64L, 65L, 66L, 67L, 68L, 69L, 70L,
            71L, 72L, 73L, 74L, 75L, 76L, 77L, 78L, 79L, 80L,
            81L, 82L, 83L, 84L, 85L, 86L, 87L, 88L, 89L, 90L,
            91L, 92L, 93L, 94L, 95L, 96L, 97L, 98L, 99L, 100L
    );

    @Transactional
    public long cleanupLoadTestData() {
        long startTime = System.currentTimeMillis();

        log.info("부하테스트 데이터 정리 시작 - testAccountIds: 1~100");

        long totalDeleted = 0;

        // 1. Direct 채팅방 정리 (테스트 계정 간의 채팅방)
        List<DirectChatRoom> directRooms = directChatRoomRepository.findByTestAccounts(TEST_ACCOUNT_IDS);

        log.info("Direct 채팅방 {} 개 발견", directRooms.size());
        for (DirectChatRoom room : directRooms) {
            totalDeleted += cleanupRoomData(room.getId(), ChatRoomType.DIRECT);
            totalDeleted += cleanupRoomMembers(room.getId(), ChatRoomType.DIRECT);
            chatAuthCacheService.removeRoom(room.getId());
            directChatRoomRepository.deleteById(room.getId());
            totalDeleted++; // 방 자체
        }

        // 2. Group 채팅방 정리 ([LOAD_TEST] 태그)
        List<GroupChatRoom> groupRooms = groupChatRoomRepository.findLoadTestRooms();

        log.info("Group 채팅방 {} 개 발견", groupRooms.size());
        for (GroupChatRoom room : groupRooms) {
            totalDeleted += cleanupRoomData(room.getId(), ChatRoomType.GROUP);
            totalDeleted += cleanupRoomMembers(room.getId(), ChatRoomType.GROUP);
            chatAuthCacheService.removeRoom(room.getId());
            groupChatRoomRepository.deleteById(room.getId());
            totalDeleted++; // 방 자체
        }

        // 3. AI 채팅방 정리 ([LOAD_TEST] 태그)
        List<AIChatRoom> aiRooms = aiChatRoomRepository.findLoadTestRooms();

        log.info("AI 채팅방 {} 개 발견", aiRooms.size());
        for (AIChatRoom room : aiRooms) {
            totalDeleted += cleanupRoomData(room.getId(), ChatRoomType.AI);
            totalDeleted += cleanupRoomMembers(room.getId(), ChatRoomType.AI);
            chatAuthCacheService.removeRoom(room.getId());
            aiChatRoomRepository.deleteById(room.getId());
            totalDeleted++; // 방 자체
        }

        long elapsedTime = System.currentTimeMillis() - startTime;

        log.info("부하테스트 데이터 정리 완료 - 총 삭제: {}개, 소요시간: {}ms", totalDeleted, elapsedTime);

        return totalDeleted;
    }

    /**
     * 채팅방의 메시지 데이터 삭제 (MongoDB)
     * @return 삭제된 메시지 수
     */
    private long cleanupRoomData(Long roomId, ChatRoomType roomType) {
        long messageCount = chatMessageRepository.countByChatRoomIdAndChatRoomType(roomId, roomType);

        if (messageCount > 0) {
            chatMessageRepository.deleteByChatRoomIdAndChatRoomType(roomId, roomType);
            log.debug("채팅방 메시지 삭제 - roomId: {}, roomType: {}, count: {}", roomId, roomType, messageCount);
        }

        return messageCount;
    }

    /**
     * 채팅방의 멤버 데이터 삭제 (MySQL)
     * @return 삭제된 멤버 수
     */
    private int cleanupRoomMembers(Long roomId, ChatRoomType roomType) {
        int memberCount = chatRoomMemberRepository.findByChatRoomIdAndChatRoomType(roomId, roomType).size();

        if (memberCount > 0) {
            chatRoomMemberRepository.deleteByChatRoomIdAndChatRoomType(roomId, roomType);
            log.debug("채팅방 멤버 삭제 - roomId: {}, roomType: {}, count: {}", roomId, roomType, memberCount);
        }

        return memberCount;
    }
}
