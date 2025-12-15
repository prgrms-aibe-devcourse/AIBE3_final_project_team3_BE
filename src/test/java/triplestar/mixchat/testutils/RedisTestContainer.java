package triplestar.mixchat.testutils;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class RedisTestContainer {

    @Container
    public static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:8.0-alpine"))
                    .withExposedPorts(6379)
                    .withReuse(true); // 컨테이너 재사용 활성화
    @DynamicPropertySource
    static void setRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port",
                () -> String.valueOf(redis.getMappedPort(6379)));
    }

    protected RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(
                redis.getHost(),
                redis.getMappedPort(6379)
        );
    }
}