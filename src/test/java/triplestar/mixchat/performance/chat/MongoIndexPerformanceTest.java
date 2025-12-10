package triplestar.mixchat.performance.chat;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;

/**
 * MongoDB 인덱스 성능 테스트
 *
 * 테스트 시나리오:
 * 1. 인덱스 확인
 * 2. 다양한 쿼리 패턴 성능 측정
 * 3. explain() 결과 분석
 *
 * 개선 포인트:
 * - createdAt 필터링 쿼리 최적화
 * - 복합 인덱스 효과 검증
 */
@SpringBootTest
@ActiveProfiles("test")
class MongoIndexPerformanceTest {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final Long TEST_ROOM_ID = 999L;
    private static final Long TEST_SENDER_ID = 888L;

    @BeforeEach
    void setUp() {
        // 기존 테스트 데이터 삭제
        Query deleteQuery = new Query(Criteria.where("chatRoomId").is(TEST_ROOM_ID));
        mongoTemplate.remove(deleteQuery, ChatMessage.class);

        // 1000개 테스트 메시지 생성
        List<ChatMessage> messages = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusDays(10);

        for (int i = 1; i <= 1000; i++) {
            ChatMessage message = new ChatMessage(
                TEST_ROOM_ID,
                TEST_SENDER_ID,
                (long) i,
                "Performance test message " + i,
                ChatMessage.MessageType.TEXT,
                ChatRoomType.GROUP,
                false
            );
            messages.add(message);
        }

        chatMessageRepository.saveAll(messages);
        System.out.println("✅ Test data created: 1000 messages");
    }

    @Test
    @DisplayName("MongoDB 인덱스 목록 확인")
    void checkMongoIndexes() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📋 MongoDB Index List");
        System.out.println("=".repeat(80));

        List<Document> indexes = mongoTemplate.getCollection("chat_messages")
                .listIndexes()
                .into(new ArrayList<>());

        indexes.forEach(index -> {
            System.out.println("\nIndex: " + index.get("name"));
            System.out.println("  Keys: " + index.get("key"));
            System.out.println("  Unique: " + index.getOrDefault("unique", false));
        });

        System.out.println("=".repeat(80) + "\n");

        assertThat(indexes).isNotEmpty();
    }

    @Test
    @DisplayName("기본 조회 쿼리 성능 (chatRoomId + chatRoomType)")
    void testBasicQueryPerformance() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("⚡ Basic Query Performance Test");
        System.out.println("=".repeat(80) + "\n");

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 기본 조회 쿼리
        List<ChatMessage> messages = chatMessageRepository
                .findByChatRoomIdAndChatRoomTypeOrderBySequenceDesc(
                    TEST_ROOM_ID,
                    ChatRoomType.GROUP,
                    PageRequest.of(0, 50)
                );

        stopWatch.stop();

        System.out.printf("Query: findByChatRoomIdAndChatRoomType%n");
        System.out.printf("Results: %d messages%n", messages.size());
        System.out.printf("Execution time: %d ms%n", stopWatch.getTotalTimeMillis());

        // Explain 실행
        printExplainResult(
            Query.query(Criteria.where("chatRoomId").is(TEST_ROOM_ID)
                    .and("chatRoomType").is("GROUP"))
                .with(PageRequest.of(0, 50))
        );

        assertThat(stopWatch.getTotalTimeMillis())
            .as("Basic query should be fast")
            .isLessThan(100);
    }

    @Test
    @DisplayName("createdAt 필터링 쿼리 성능 (인덱스 최적화 대상)")
    void testCreatedAtFilterQueryPerformance() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📅 CreatedAt Filter Query Performance Test");
        System.out.println("=".repeat(80) + "\n");

        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // createdAt 필터링 쿼리
        List<ChatMessage> messages = chatMessageRepository
                .findByChatRoomIdAndChatRoomTypeAndCreatedAtGreaterThanEqualOrderBySequenceDesc(
                    TEST_ROOM_ID,
                    ChatRoomType.GROUP,
                    threeDaysAgo,
                    PageRequest.of(0, 50)
                );

        stopWatch.stop();

        System.out.printf("Query: findByChatRoomIdAndChatRoomTypeAndCreatedAtGreaterThan%n");
        System.out.printf("Filter: createdAt >= %s%n", threeDaysAgo);
        System.out.printf("Results: %d messages%n", messages.size());
        System.out.printf("Execution time: %d ms%n", stopWatch.getTotalTimeMillis());

        // Explain 실행
        printExplainResult(
            Query.query(Criteria.where("chatRoomId").is(TEST_ROOM_ID)
                    .and("chatRoomType").is("GROUP")
                    .and("createdAt").gte(threeDaysAgo))
                .with(PageRequest.of(0, 50))
        );

        System.out.println("\n💡 Optimization Tip:");
        System.out.println("   If execution stage is COLLSCAN, add compound index:");
        System.out.println("   db.chat_messages.createIndex({");
        System.out.println("       chatRoomId: 1,");
        System.out.println("       chatRoomType: 1,");
        System.out.println("       createdAt: 1,");
        System.out.println("       sequence: -1");
        System.out.println("   })");
        System.out.println("=".repeat(80) + "\n");
    }

    @Test
    @DisplayName("페이지네이션 성능 테스트 (대량 skip)")
    void testPaginationPerformance() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📄 Pagination Performance Test");
        System.out.println("=".repeat(80) + "\n");

        // Page 0 (처음)
        StopWatch sw1 = new StopWatch();
        sw1.start();
        chatMessageRepository.findByChatRoomIdAndChatRoomTypeOrderBySequenceDesc(
            TEST_ROOM_ID, ChatRoomType.GROUP, PageRequest.of(0, 20)
        );
        sw1.stop();

        // Page 10 (skip 200)
        StopWatch sw2 = new StopWatch();
        sw2.start();
        chatMessageRepository.findByChatRoomIdAndChatRoomTypeOrderBySequenceDesc(
            TEST_ROOM_ID, ChatRoomType.GROUP, PageRequest.of(10, 20)
        );
        sw2.stop();

        // Page 40 (skip 800)
        StopWatch sw3 = new StopWatch();
        sw3.start();
        chatMessageRepository.findByChatRoomIdAndChatRoomTypeOrderBySequenceDesc(
            TEST_ROOM_ID, ChatRoomType.GROUP, PageRequest.of(40, 20)
        );
        sw3.stop();

        System.out.printf("Page 0 (skip 0)  : %d ms%n", sw1.getTotalTimeMillis());
        System.out.printf("Page 10 (skip 200): %d ms%n", sw2.getTotalTimeMillis());
        System.out.printf("Page 40 (skip 800): %d ms%n", sw3.getTotalTimeMillis());

        System.out.println("\n⚠️  Note: Large skip values can be slow in MongoDB");
        System.out.println("   Consider using lastMessageId-based cursor pagination");
        System.out.println("=".repeat(80) + "\n");
    }

    /**
     * MongoDB explain() 결과 출력
     */
    private void printExplainResult(Query query) {
        try {
            Document explainCommand = new Document("explain",
                new Document("find", "chat_messages")
                    .append("filter", query.getQueryObject())
                    .append("sort", query.getSortObject())
                    .append("limit", 50)
            );

            Document explainResult = mongoTemplate.executeCommand(explainCommand);
            Document executionStats = explainResult.get("executionStats", Document.class);

            if (executionStats != null) {
                System.out.println("\n📊 Explain Result:");
                System.out.println("=".repeat(80));
                System.out.printf("Execution Stage   : %s%n",
                    executionStats.get("executionStages", Document.class).getString("stage"));
                System.out.printf("Execution Time    : %d ms%n",
                    executionStats.getInteger("executionTimeMillis"));
                System.out.printf("Total Docs Examined: %d%n",
                    executionStats.getInteger("totalDocsExamined"));
                System.out.printf("Total Keys Examined: %d%n",
                    executionStats.getInteger("totalKeysExamined"));
                System.out.printf("Docs Returned     : %d%n",
                    executionStats.getInteger("nReturned"));

                // 인덱스 사용 여부
                String stage = executionStats.get("executionStages", Document.class)
                        .getString("stage");
                if ("IXSCAN".equals(stage)) {
                    System.out.println("\n✅ Index is being used (IXSCAN)");
                } else if ("COLLSCAN".equals(stage)) {
                    System.out.println("\n❌ Full collection scan (COLLSCAN) - Add index!");
                }
                System.out.println("=".repeat(80));
            }
        } catch (Exception e) {
            System.out.println("⚠️  Could not run explain: " + e.getMessage());
        }
    }
}
