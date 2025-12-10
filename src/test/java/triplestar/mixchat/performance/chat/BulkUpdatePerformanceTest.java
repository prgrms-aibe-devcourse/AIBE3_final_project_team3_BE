package triplestar.mixchat.performance.chat;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.GroupChatRoom;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.chat.repository.GroupChatRoomRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import java.util.stream.LongStream;
import triplestar.mixchat.performance.chat.config.PerformanceTestConfig;
import triplestar.mixchat.performance.chat.util.PerformanceMeasurement;

/**
 * Bulk Update 쿼리 성능 테스트
 *
 * 읽음 처리(lastReadSequence 업데이트) 성능 측정
 *
 * 시나리오:
 * 1. 소규모 (10명)
 * 2. 중규모 (100명)
 * 3. 대규모 (1000명)
 *
 * 측정 지표:
 * - 실행 시간
 * - 쿼리 실행 횟수
 * - 영향받은 row 수
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(PerformanceTestConfig.class)
@Transactional
class BulkUpdatePerformanceTest {

    @Autowired
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Autowired
    private GroupChatRoomRepository groupChatRoomRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private Statistics statistics;

    private GroupChatRoom testRoom;

    @BeforeEach
    void setUp() {
        // Flyway로 생성된 기존 테스트 유저 활용 (test1@test.com)
        Member owner = memberRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Test user 1 not found"));

        testRoom = GroupChatRoom.create(
                "Performance Test Room",
                "Test description",
                "TEST",
                null,
                owner
        );
        testRoom = groupChatRoomRepository.save(testRoom);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Bulk Update 성능 - 소규모 (10명)")
    void testBulkUpdate_SmallScale() {
        int memberCount = 10;
        testBulkUpdatePerformance("Small Scale (10 members)", memberCount);
    }

    @Test
    @DisplayName("Bulk Update 성능 - 중규모 (100명)")
    void testBulkUpdate_MediumScale() {
        int memberCount = 100;
        testBulkUpdatePerformance("Medium Scale (100 members)", memberCount);
    }

    @Test
    @DisplayName("Bulk Update 성능 - 대규모 (1000명)")
    void testBulkUpdate_LargeScale() {
        int memberCount = 1000;
        testBulkUpdatePerformance("Large Scale (1000 members)", memberCount);
    }

    /**
     * Bulk Update 성능 측정 헬퍼 메서드
     */
    private void testBulkUpdatePerformance(String testName, int memberCount) {
        System.out.println("\n" + "=".repeat(80));
        System.out.printf("🔧 Bulk Update Performance Test - %s%n", testName);
        System.out.println("=".repeat(80) + "\n");

        // 1. 멤버 생성 및 채팅방 참가
        List<Member> members = createMembers(memberCount);
        Set<Long> memberIds = new HashSet<>();

        for (Member member : members) {
            ChatMember chatMember = new ChatMember(
                member,
                testRoom.getId(),
                ChatRoomType.GROUP
            );
            chatRoomMemberRepository.save(chatMember);
            memberIds.add(member.getId());
        }

        entityManager.flush();
        entityManager.clear();

        System.out.printf("✅ Created %d members and added to chat room%n%n", memberCount);

        // 2. Bulk Update 성능 측정
        Long targetSequence = 1000L;
        LocalDateTime now = LocalDateTime.now();

        PerformanceMeasurement result = PerformanceMeasurement.measure(
            testName,
            statistics,
            () -> {
                int updated = chatRoomMemberRepository.bulkUpdateLastReadSequence(
                    testRoom.getId(),
                    ChatRoomType.GROUP,
                    memberIds,
                    targetSequence,
                    now
                );

                System.out.printf("📝 Updated %d records%n", updated);
            }
        );

        result.printResult();

        // 3. 성능 분석
        System.out.println("\n📊 Performance Analysis");
        System.out.println("=".repeat(80));
        System.out.printf("Members updated   : %d%n", memberCount);
        System.out.printf("Time per member   : %.2f ms%n",
            (double) result.getExecutionTimeMs() / memberCount);
        System.out.printf("Throughput        : %.2f updates/sec%n",
            (double) memberCount / result.getExecutionTimeMs() * 1000);

        // 성능 기준
        long acceptableTime = memberCount < 100 ? 100 : memberCount;
        if (result.getExecutionTimeMs() < acceptableTime) {
            System.out.println("\n✅ EXCELLENT - Bulk update is efficient");
        } else if (result.getExecutionTimeMs() < acceptableTime * 2) {
            System.out.println("\n⚠️  ACCEPTABLE - Consider optimization for larger scale");
        } else {
            System.out.println("\n❌ POOR - Optimization required");
            System.out.println("   Suggestions:");
            System.out.println("   - Check index on (chat_room_id, chat_room_type, member_id)");
            System.out.println("   - Consider batch size optimization");
        }

        System.out.println("=".repeat(80) + "\n");

        // 검증
        assertThat(result.getQueryCount())
            .as("Bulk update should use minimal queries")
            .isLessThan(10);
    }

    @Test
    @DisplayName("개별 UPDATE vs Bulk UPDATE 비교")
    void compareSingleUpdateVsBulkUpdate() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("⚖️  Single UPDATE vs Bulk UPDATE Comparison (100 members)");
        System.out.println("=".repeat(80) + "\n");

        int memberCount = 100;
        List<Member> members = createMembers(memberCount);
        List<ChatMember> chatMembers = new ArrayList<>();

        for (Member member : members) {
            ChatMember chatMember = new ChatMember(
                member,
                testRoom.getId(),
                ChatRoomType.GROUP
            );
            chatRoomMemberRepository.save(chatMember);
            chatMembers.add(chatMember);
        }

        entityManager.flush();
        entityManager.clear();

        Long targetSequence = 1000L;
        LocalDateTime now = LocalDateTime.now();

        // 1. Single UPDATE (100번 개별 실행)
        PerformanceMeasurement singleResult = PerformanceMeasurement.measure(
            "Single UPDATE x100",
            statistics,
            () -> {
                for (ChatMember member : chatMembers) {
                    member.updateLastReadSequence(targetSequence);
                    chatRoomMemberRepository.save(member);
                }
                entityManager.flush();
            }
        );

        singleResult.printResult();

        // 2. Bulk UPDATE (한 번에 실행)
        entityManager.clear();
        Set<Long> memberIds = new HashSet<>();
        chatMembers.forEach(cm -> memberIds.add(cm.getMember().getId()));

        PerformanceMeasurement bulkResult = PerformanceMeasurement.measure(
            "Bulk UPDATE x1",
            statistics,
            () -> {
                chatRoomMemberRepository.bulkUpdateLastReadSequence(
                    testRoom.getId(),
                    ChatRoomType.GROUP,
                    memberIds,
                    targetSequence,
                    now
                );
            }
        );

        bulkResult.printResult();

        // 3. 비교
        PerformanceMeasurement.compareResults(singleResult, bulkResult);

        // 검증
        assertThat(bulkResult.getExecutionTimeMs())
            .as("Bulk update should be significantly faster")
            .isLessThan(singleResult.getExecutionTimeMs() / 2);
    }

    /**
     * 테스트용 멤버 조회 (Flyway로 생성된 test1~test100 유저 활용)
     */
    private List<Member> createMembers(int count) {
        if (count > 100) {
            throw new IllegalArgumentException(
                "Test requires max 100 members. Flyway provides test users 1-100. Requested: " + count
            );
        }

        // Flyway로 생성된 유저 재사용 (Member 생성 오버헤드 제거)
        return memberRepository.findAllById(
            LongStream.rangeClosed(2, count + 1).boxed().toList()  // ID 2부터 (1은 owner)
        );
    }
}
