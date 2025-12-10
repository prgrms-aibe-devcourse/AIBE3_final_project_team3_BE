package triplestar.mixchat.performance.chat;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
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
import triplestar.mixchat.domain.chat.chat.entity.DirectChatRoom;
import triplestar.mixchat.domain.chat.chat.repository.DirectChatRoomRepository;
import triplestar.mixchat.domain.chat.chat.service.ChatSequenceGenerator;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.performance.chat.config.PerformanceTestConfig;
import triplestar.mixchat.performance.chat.util.PerformanceMeasurement;

/**
 * Sequence 생성 성능 비교 테스트
 *
 * Before: DB Pessimistic Lock (findByIdWithLock)
 * After:  Redis INCR (ChatSequenceGenerator)
 *
 * 측정 지표:
 * - 실행 시간
 * - 쿼리 실행 횟수
 * - DB 커넥션 사용량
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(PerformanceTestConfig.class)
@Transactional
class SequenceGenerationPerformanceTest {

    @Autowired
    private ChatSequenceGenerator sequenceGenerator;

    @Autowired
    private DirectChatRoomRepository directChatRoomRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private Statistics statistics;

    private DirectChatRoom testRoom;

    @BeforeEach
    void setUp() {
        // Flyway로 생성된 기존 테스트 유저 활용 (test1, test2)
        Member user1 = memberRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Test user 1 not found"));
        Member user2 = memberRepository.findById(2L)
                .orElseThrow(() -> new IllegalStateException("Test user 2 not found"));

        // 테스트용 채팅방 생성
        testRoom = DirectChatRoom.create(user1, user2);
        testRoom = directChatRoomRepository.save(testRoom);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Redis INCR vs DB Pessimistic Lock - 단일 요청 성능 비교")
    void compareSequenceGenerationPerformance_Single() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔬 Sequence Generation Performance Test - Single Request");
        System.out.println("=".repeat(80) + "\n");

        // 1. Before: DB Pessimistic Lock 방식
        PerformanceMeasurement beforeResult = PerformanceMeasurement.measure(
            "Before (DB Pessimistic Lock)",
            statistics,
            () -> {
                DirectChatRoom room = directChatRoomRepository.findByIdWithLock(testRoom.getId())
                        .orElseThrow();
                room.generateNextSequence();
                entityManager.flush();
            }
        );

        beforeResult.printResult();

        // EntityManager 초기화 (공정한 비교)
        entityManager.clear();

        // 2. After: Redis INCR 방식
        PerformanceMeasurement afterResult = PerformanceMeasurement.measure(
            "After (Redis INCR)",
            statistics,
            () -> {
                sequenceGenerator.generateSequence(testRoom.getId(), ChatRoomType.DIRECT);
            }
        );

        afterResult.printResult();

        // 3. 비교 결과 출력
        PerformanceMeasurement.compareResults(beforeResult, afterResult);

        // 4. 검증: Redis 방식이 더 빨라야 함
        assertThat(afterResult.getExecutionTimeMs())
            .as("Redis INCR should be faster than DB lock")
            .isLessThanOrEqualTo(beforeResult.getExecutionTimeMs());
    }

    @Test
    @DisplayName("Redis INCR vs DB Pessimistic Lock - 연속 100회 요청 성능 비교")
    void compareSequenceGenerationPerformance_Batch() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔬 Sequence Generation Performance Test - 100 Consecutive Requests");
        System.out.println("=".repeat(80) + "\n");

        final int ITERATION = 100;

        // 1. Before: DB Pessimistic Lock 방식 (100회)
        PerformanceMeasurement beforeResult = PerformanceMeasurement.measure(
            "Before (DB Lock x100)",
            statistics,
            () -> {
                for (int i = 0; i < ITERATION; i++) {
                    DirectChatRoom room = directChatRoomRepository.findByIdWithLock(testRoom.getId())
                            .orElseThrow();
                    room.generateNextSequence();
                    entityManager.flush();
                    entityManager.clear(); // 캐시 초기화
                }
            }
        );

        beforeResult.printResult();

        // 2. After: Redis INCR 방식 (100회)
        PerformanceMeasurement afterResult = PerformanceMeasurement.measure(
            "After (Redis INCR x100)",
            statistics,
            () -> {
                for (int i = 0; i < ITERATION; i++) {
                    sequenceGenerator.generateSequence(testRoom.getId(), ChatRoomType.DIRECT);
                }
            }
        );

        afterResult.printResult();

        // 3. 비교 결과 출력
        PerformanceMeasurement.compareResults(beforeResult, afterResult);

        // 4. 처리량 계산
        double beforeTps = (double) ITERATION / beforeResult.getExecutionTimeMs() * 1000;
        double afterTps = (double) ITERATION / afterResult.getExecutionTimeMs() * 1000;

        System.out.println("\n📈 Throughput Analysis");
        System.out.println("=".repeat(80));
        System.out.printf("Before TPS: %.2f sequences/sec%n", beforeTps);
        System.out.printf("After TPS : %.2f sequences/sec%n", afterTps);
        System.out.printf("Improvement: %.1fx faster%n", afterTps / beforeTps);
        System.out.println("=".repeat(80) + "\n");

        // 5. 검증
        assertThat(afterResult.getExecutionTimeMs())
            .as("Redis INCR batch should be significantly faster")
            .isLessThan(beforeResult.getExecutionTimeMs() / 2); // 최소 2배 이상 빨라야 함
    }

    @Test
    @DisplayName("Redis INCR - 대량 요청 처리 성능 (1000회)")
    void testRedisIncrHighLoad() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🚀 Redis INCR High Load Test - 1000 Requests");
        System.out.println("=".repeat(80) + "\n");

        final int ITERATION = 1000;

        PerformanceMeasurement result = PerformanceMeasurement.measure(
            "Redis INCR x1000",
            statistics,
            () -> {
                for (int i = 0; i < ITERATION; i++) {
                    sequenceGenerator.generateSequence(testRoom.getId(), ChatRoomType.DIRECT);
                }
            }
        );

        result.printResult();

        // TPS 계산
        double tps = (double) ITERATION / result.getExecutionTimeMs() * 1000;
        System.out.println("\n📊 Performance Metrics");
        System.out.println("=".repeat(80));
        System.out.printf("Total Requests  : %,d%n", ITERATION);
        System.out.printf("Total Time      : %,d ms%n", result.getExecutionTimeMs());
        System.out.printf("Avg Time/Request: %.2f ms%n", (double) result.getExecutionTimeMs() / ITERATION);
        System.out.printf("Throughput (TPS): %.2f sequences/sec%n", tps);
        System.out.println("=".repeat(80) + "\n");

        // 검증: 1000 TPS 이상 나와야 함 (실무 기준)
        assertThat(tps)
            .as("Should handle at least 1000 TPS")
            .isGreaterThan(1000);
    }
}
