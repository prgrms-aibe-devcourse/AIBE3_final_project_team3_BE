package triplestar.mixchat.performance.chat;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.dto.MessagePageResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.entity.DirectChatRoom;
import triplestar.mixchat.domain.chat.chat.repository.DirectChatRoomRepository;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.performance.chat.config.PerformanceTestConfig;
import triplestar.mixchat.performance.chat.util.PerformanceMeasurement;

/**
 * N+1 문제 탐지 테스트
 *
 * 채팅 메시지 조회 시 발생할 수 있는 N+1 문제를 감지하고 측정
 *
 * 시나리오:
 * 1. 50개 메시지 조회
 * 2. 각 메시지의 sender 정보 접근
 * 3. 쿼리 실행 횟수 확인
 *
 * 예상:
 * - N+1 있음: 51개 쿼리 (1 + 50)
 * - N+1 없음: 1-2개 쿼리 (Fetch Join 사용)
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(PerformanceTestConfig.class)
@Transactional
class NPlusOneDetectionTest {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private DirectChatRoomRepository directChatRoomRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private Statistics statistics;

    private DirectChatRoom testRoom;
    private Member sender;
    private List<String> messageIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // Flyway로 생성된 기존 테스트 유저 활용 (test1, test2)
        sender = memberRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Test user 1 not found"));
        Member receiver = memberRepository.findById(2L)
                .orElseThrow(() -> new IllegalStateException("Test user 2 not found"));

        // 테스트 채팅방 생성
        testRoom = DirectChatRoom.create(sender, receiver);
        testRoom = directChatRoomRepository.save(testRoom);

        // 50개 메시지 생성
        for (int i = 1; i <= 50; i++) {
            ChatMessage message = new ChatMessage(
                testRoom.getId(),
                sender.getId(),
                (long) i,
                "Test message " + i,
                ChatMessage.MessageType.TEXT,
                ChatRoomType.DIRECT,
                false
            );
            ChatMessage saved = chatMessageRepository.save(message);
            messageIds.add(saved.getId());
        }

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("N+1 문제 탐지 - 메시지 조회")
    void detectNPlusOneProblem_MessageRetrieval() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔍 N+1 Problem Detection - Message Retrieval (50 messages)");
        System.out.println("=".repeat(80) + "\n");

        PerformanceMeasurement result = PerformanceMeasurement.measure(
            "Get Messages (50 records)",
            statistics,
            () -> {
                // 메시지 50개 조회
                List<ChatMessage> messages = chatMessageRepository
                    .findByChatRoomIdAndChatRoomTypeOrderBySequenceDesc(
                        testRoom.getId(),
                        ChatRoomType.DIRECT,
                        PageRequest.of(0, 50)
                    );

                // Lazy loading 발생 시키기 (N+1 유발)
                messages.forEach(msg -> {
                    // sender 정보 접근 시 추가 쿼리 발생
                    String senderEmail = msg.getSenderId().toString();
                });
            }
        );

        result.printResult();

        // N+1 판단
        System.out.println("\n🔬 N+1 Problem Analysis");
        System.out.println("=".repeat(80));
        System.out.printf("Expected queries (no N+1): 1-2 queries%n");
        System.out.printf("Expected queries (with N+1): 51+ queries (1 + 50)%n");
        System.out.printf("Actual queries: %d queries%n%n", result.getQueryCount());

        if (result.getQueryCount() > 10) {
            System.out.println("❌ N+1 PROBLEM DETECTED!");
            System.out.println("   Solution: Use Fetch Join or @EntityGraph");
            System.out.println("   Example: @Query(\"SELECT m FROM ChatMessage m JOIN FETCH m.sender WHERE ...\")");
        } else {
            System.out.println("✅ NO N+1 PROBLEM - Queries are optimized");
        }
        System.out.println("=".repeat(80) + "\n");
    }

    @Test
    @DisplayName("ChatMessageService.getMessages() N+1 탐지")
    void detectNPlusOneProblem_ServiceLayer() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔍 N+1 Problem Detection - Service Layer (getMessages API)");
        System.out.println("=".repeat(80) + "\n");

        PerformanceMeasurement result = PerformanceMeasurement.measure(
            "ChatMessageService.getMessagesWithSenderInfo",
            statistics,
            () -> {
                // 실제 API 호출과 동일한 방식
                MessagePageResp response = chatMessageService.getMessagesWithSenderInfo(
                    testRoom.getId(),
                    ChatRoomType.DIRECT,
                    sender.getId(),
                    null, // lastMessageId
                    20    // pageSize
                );

                // 모든 메시지의 필드 접근 (Lazy loading 유발)
                response.messages().forEach(msg -> {
                    msg.senderId();
                    msg.sender();
                    msg.content();
                });
            }
        );

        result.printResult();

        // 분석
        System.out.println("\n📊 Service Layer Query Analysis");
        System.out.println("=".repeat(80));
        System.out.printf("Messages fetched: 20%n");
        System.out.printf("Queries executed: %d%n", result.getQueryCount());
        System.out.printf("Queries per message: %.2f%n%n",
            (double) result.getQueryCount() / 20);

        if (result.getQueryCount() > 5) {
            System.out.println("⚠️  WARNING: High query count detected");
            System.out.println("   This may indicate N+1 problem or missing indexes");
        } else {
            System.out.println("✅ Query count is acceptable");
        }
        System.out.println("=".repeat(80) + "\n");
    }

    @Test
    @DisplayName("대량 메시지 조회 성능 테스트 (100개)")
    void testLargeMessageRetrieval() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📈 Large Message Retrieval Performance - 100 Messages");
        System.out.println("=".repeat(80) + "\n");

        // 추가 50개 메시지 생성 (총 100개)
        for (int i = 51; i <= 100; i++) {
            ChatMessage message = new ChatMessage(
                testRoom.getId(),
                sender.getId(),
                (long) i,
                "Test message " + i,
                ChatMessage.MessageType.TEXT,
                ChatRoomType.DIRECT,
                false
            );
            chatMessageRepository.save(message);
        }
        entityManager.flush();
        entityManager.clear();

        PerformanceMeasurement result = PerformanceMeasurement.measure(
            "Get 100 Messages",
            statistics,
            () -> {
                List<ChatMessage> messages = chatMessageRepository
                    .findByChatRoomIdAndChatRoomTypeOrderBySequenceDesc(
                        testRoom.getId(),
                        ChatRoomType.DIRECT,
                        PageRequest.of(0, 100)
                    );

                // 전체 필드 접근
                messages.forEach(msg -> {
                    msg.getSenderId();
                    msg.getContent();
                    msg.getSequence();
                });
            }
        );

        result.printResult();

        // 성능 기준 검증
        System.out.println("\n⏱️  Performance Criteria");
        System.out.println("=".repeat(80));
        System.out.printf("Target: < 100ms for 100 messages%n");
        System.out.printf("Actual: %d ms%n%n", result.getExecutionTimeMs());

        if (result.getExecutionTimeMs() < 100) {
            System.out.println("✅ EXCELLENT - Meets performance target");
        } else if (result.getExecutionTimeMs() < 500) {
            System.out.println("⚠️  ACCEPTABLE - Consider optimization");
        } else {
            System.out.println("❌ POOR - Optimization required");
        }
        System.out.println("=".repeat(80) + "\n");

        // 검증
        assertThat(result.getQueryCount())
            .as("Should not have excessive queries")
            .isLessThan(20); // 100개 메시지에 20개 이상 쿼리는 비효율적
    }
}
