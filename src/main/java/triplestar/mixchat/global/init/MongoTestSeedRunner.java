package triplestar.mixchat.global.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.chat.chat.constant.ChatRoomType;
import triplestar.mixchat.domain.chat.chat.entity.ChatMessage;
import triplestar.mixchat.domain.chat.chat.service.ChatMessageService;
import triplestar.mixchat.domain.chat.chat.service.DirectChatRoomService;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;

/**
 * 테스트 전용: 애플리케이션 시작 시 MongoDB를 드롭하고
 * 1번/2번 유저 간 Direct 채팅방에 200개의 메시지를 시드합니다.
 * 운영 배포 전 반드시 제거하세요.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MongoTestSeedRunner implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;
    private final DirectChatRoomService directChatRoomService;
    private final ChatMessageService chatMessageService;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.warn("[seed] Dropping MongoDB and seeding 200 messages between members 1 and 2");

        try {
            mongoTemplate.getDb().drop();
        } catch (Exception e) {
            log.error("[seed] Failed to drop Mongo DB, skipping seed", e);
            return;
        }

        var member1 = memberRepository.findById(1L).orElse(null);
        var member2 = memberRepository.findById(2L).orElse(null);

        if (member1 == null || member2 == null) {
            log.warn("[seed] Members 1 or 2 not found, skipping seed");
            return;
        }

        Long roomId = directChatRoomService
                .findOrCreateDirectChatRoom(member1.getId(), member2.getId(), member1.getNickname())
                .id();

        String nick1 = member1.getNickname();
        String nick2 = member2.getNickname();

        for (int i = 1; i <= 200; i++) {
            boolean isUser1Turn = ((i - 1) / 10) % 2 == 0; // 10개씩 교대
            Long senderId = isUser1Turn ? member1.getId() : member2.getId();
            String senderNickname = isUser1Turn ? nick1 : nick2;

            chatMessageService.saveMessage(
                    roomId,
                    senderId,
                    senderNickname,
                    "seed message " + i,
                    ChatMessage.MessageType.TEXT,
                    ChatRoomType.DIRECT,
                    false
            );
        }

        log.info("[seed] Seeded 200 messages into room {}", roomId);
    }
}
