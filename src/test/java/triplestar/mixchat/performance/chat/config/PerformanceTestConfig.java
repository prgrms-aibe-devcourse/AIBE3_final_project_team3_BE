package triplestar.mixchat.performance.chat.config;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 성능 측정용 테스트 설정
 * Hibernate Statistics를 활성화하여 쿼리 실행 시간, 횟수 등을 측정
 */
@TestConfiguration
public class PerformanceTestConfig {

    /**
     * Hibernate Statistics 활성화
     * - 쿼리 실행 횟수
     * - 쿼리 실행 시간
     * - 2차 캐시 히트율
     * - Connection pool 사용량
     */
    @Bean
    public Statistics hibernateStatistics(EntityManagerFactory emf) {
        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        return statistics;
    }
}
