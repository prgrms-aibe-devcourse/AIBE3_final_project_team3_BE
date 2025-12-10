package triplestar.mixchat.performance.chat;

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
import triplestar.mixchat.domain.chat.chat.dto.CreateGroupChatReq;
import triplestar.mixchat.domain.chat.chat.dto.GroupChatRoomResp;
import triplestar.mixchat.domain.chat.chat.entity.ChatMember;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.entity.DirectChatRoom;
import triplestar.mixchat.domain.chat.chat.repository.ChatMessageRepository;
import triplestar.mixchat.domain.chat.chat.repository.ChatRoomMemberRepository;
import triplestar.mixchat.domain.chat.chat.repository.DirectChatRoomRepository;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.service.GroupChatRoomService;
import triplestar.mixchat.domain.member.member.entity.Member;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.performance.chat.config.PerformanceTestConfig;
import triplestar.mixchat.performance.chat.util.PerformanceMeasurement;
import triplestar.mixchat.testutils.TestMemberFactory;

/**
 * N+1 문제 탐지 테스트
 *
 * 채팅 메시지 조회 및 그룹 채팅방 참여 시 발생할 수 있는 N+1 문제를 감지하고 측정
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(PerformanceTestConfig.class)
@Transactional
class NPlusOneDetectionTest {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private GroupChatRoomService groupChatRoomService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private DirectChatRoomRepository directChatRoomRepository;

    @Autowired
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private Statistics statistics;

    private DirectChatRoom testRoom;
    private Member sender;

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리
        memberRepository.deleteAll();
        
        // 테스트용 멤버 생성
        sender = memberRepository.save(TestMemberFactory.createMember("sender"));
        Member receiver = memberRepository.save(TestMemberFactory.createMember("receiver"));

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
            chatMessageRepository.save(message);
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

                // Lazy loading 발생 시키기 (N+1 유발 가능성)
                messages.forEach(msg -> {
                    String senderInfo = msg.getSenderId().toString();
                });
            }
        );

        result.printResult();
        
        // MongoDB 조회이므로 JPA N+1과는 다르게 쿼리 수가 적게 나와야 함 (1개)
        System.out.println("Queries: " + result.getQueryCount());
    }

    @Test
    @DisplayName("N+1 문제 탐지 - 그룹 채팅방 참여 (멤버 목록 조회)")
    void detectNPlusOneProblem_JoinGroupRoom() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔍 N+1 Problem Detection - Join Group Room (20 members)");
        System.out.println("=".repeat(80) + "\n");

        // 1. 테스트용 멤버 20명 추가 생성 및 저장
        List<Member> members = new ArrayList<>();
        // 방장 포함 20명
        for (int i = 1; i <= 20; i++) {
            Member m = memberRepository.save(TestMemberFactory.createMember("groupMember" + i));
            members.add(m);
        }
        
        entityManager.flush();

        // 2. 그룹 채팅방 생성 (방장: members[0])
        CreateGroupChatReq createReq = new CreateGroupChatReq(
            "N+1 Test Room", new ArrayList<>(), null, "Desc", "Topic"
        );
        GroupChatRoomResp createdRoom = groupChatRoomService.createGroupRoom(createReq, members.get(0).getId());
        Long roomId = createdRoom.id();

        // 3. 19명 멤버 추가 (참여 처리) - Fetch Join 없이 저장
        for (int i = 1; i < 20; i++) {
             ChatMember cm = new ChatMember(members.get(i), roomId, ChatRoomType.GROUP);
             chatRoomMemberRepository.save(cm);
        }
        
        entityManager.flush();
        entityManager.clear(); // 영속성 컨텍스트 초기화 (중요)

        // 4. 새로운 멤버가 참여 시도 -> 여기서 N+1 발생 예상
        Member newJoiner = memberRepository.save(TestMemberFactory.createMember("newJoiner"));
        
        PerformanceMeasurement result = PerformanceMeasurement.measure(
            "Join Group Room (return member list)",
            statistics,
            () -> {
                // joinGroupRoom 내부에서 멤버 리스트를 DTO로 변환하며 N+1 발생 가능성
                groupChatRoomService.joinGroupRoom(roomId, newJoiner.getId(), null);
            }
        );

        result.printResult();

        // 5. 분석
        System.out.println("\n🔬 Analysis");
        // 최적화 된 경우: 
        // 1. 방 조회 
        // 2. 멤버 여부 확인
        // 3. 비밀번호 확인(여기선 패스)
        // 4. 새 멤버 조회
        // 5. 새 멤버 저장
        // 6. 전체 멤버 조회 (Fetch Join 사용시 1쿼리, 미사용시 1 + 20쿼리)
        // => 총 6~7개 내외여야 함.
        
        System.out.printf("Expected queries (Optimized): < 10 queries%n");
        System.out.printf("Actual queries: %d queries%n", result.getQueryCount());

        if (result.getQueryCount() > 15) {
            System.out.println("❌ N+1 PROBLEM DETECTED! (Member list fetch without Join)");
            System.out.println("   Solution: Use joinGroupRoom -> chatRoomMemberRepository.findAllByRoomIdsWithMember()");
        } else {
            System.out.println("✅ OPTIMIZED or Low impact");
        }
        System.out.println("=".repeat(80) + "\n");
    }
}
