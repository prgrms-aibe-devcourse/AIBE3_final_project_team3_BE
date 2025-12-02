package triplestar.mixchat.domain.member.presence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import triplestar.mixchat.domain.member.member.repository.MemberRepository;
import triplestar.mixchat.domain.member.presence.repository.PresenceRepository;
import triplestar.mixchat.testutils.RedisTestContainer;

@Transactional
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class PresenceServiceTest extends RedisTestContainer {

    StringRedisTemplate stringRedisTemplate;
    RedisConnectionFactory redisConnectionFactory;

    PresenceRepository presenceRepository;
    PresenceService presenceService;
    @Mock
    MemberRepository memberMockRepository;

    @BeforeEach
    void setUp() {
        redis.start();
        redisConnectionFactory = redisConnectionFactory();

        LettuceConnectionFactory lettuce = (LettuceConnectionFactory) redisConnectionFactory;
        lettuce.afterPropertiesSet();
        lettuce.start();

        stringRedisTemplate = new StringRedisTemplate(redisConnectionFactory);
        stringRedisTemplate.afterPropertiesSet();

        presenceRepository = new PresenceRepository(
                stringRedisTemplate,
                "test:presence-user:",
                2
        );
        presenceService = new PresenceService(presenceRepository, memberMockRepository);
    }

    @Test
    @DisplayName("heartbeat 호출 시 Redis에 사용자 접속 정보가 저장된다.")
    void heartbeat_saves_presence_in_redis() {
        // given
        Long memberId = 1L;

        // when
        presenceService.heartbeat(memberId);

        // then
        String key = "test:presence-user:";
        Double score = stringRedisTemplate.opsForZSet()
                .score(key, memberId.toString());

        assertThat(score).isNotNull();
        assertThat(score).isGreaterThan(0);
    }

    @Test
    @DisplayName("TTL이 만료된 후에는 사용자가 오프라인 상태로 간주된다.")
    void presence_expires_after_ttl() throws InterruptedException {
        // given
        Long memberId = 2L;
        presenceService.heartbeat(memberId);

        // when
        Thread.sleep(3000); // TTL(3초) 이후 대기

        // then
        String key = "test:presence-user:" + memberId;
        Boolean exists = stringRedisTemplate.hasKey(key);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("isOnlineBulk 메서드는 여러 사용자의 온라인 상태를 반환한다.")
    void filterIsOnline_returns_correct_status() {
        // given
        Long onlineMemberId = 3L;
        Long offlineMemberId = 4L;
        presenceService.heartbeat(onlineMemberId);

        // when
        Set<Long> result = presenceService.filterIsOnline(
                List.of(onlineMemberId, offlineMemberId)
        );

        // then
        assertThat(result.contains(onlineMemberId)).isTrue();
        assertThat(result.contains(offlineMemberId)).isFalse();
    }

    @Test
    @DisplayName("getOnlineMemberIds 메서드는 온라인 상태인 사용자 ID 목록을 반환한다.")
    void getOnlineMemberIds_returns_online_member_ids() {
        // given
        Long memberId1 = 5L;
        Long memberId2 = 6L;
        Long memberId3 = 7L;
        presenceService.heartbeat(memberId1);
        presenceService.heartbeat(memberId2);

        // when
        List<Long> onlineIds = presenceService.getOnlineMemberIds(0, 10);

        // then
        assertThat(onlineIds).contains(memberId1, memberId2);
        assertThat(onlineIds).doesNotContain(memberId3);
    }

    @Test
    @DisplayName("cleanupExpired는 만료된 사용자만 MemberRepository.updateLastSeenAt을 호출한다.")
    void cleanupExpired_test() throws InterruptedException {
        // given
        Long memberId1 = 8L;
        Long memberId2 = 9L;

        // TTL 2초
        presenceService.heartbeat(memberId1);
        Thread.sleep(1500);                   // 1.5초 대기
        presenceService.heartbeat(memberId2);
        Thread.sleep(1000);                   // id1 만료(2.5초 경과), id2는 유효(1.0초 경과)

        // when
        presenceService.removeExpiredEntries();

        // then
        // memberId1 만 업데이트 호출됨
        verify(memberMockRepository, times(1))
                .updateLastSeenAt(eq(memberId1), any());
        verify(memberMockRepository, never())
                .updateLastSeenAt(eq(memberId2), any());
    }
}