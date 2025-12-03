package triplestar.mixchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableJpaAuditing
@EnableMongoAuditing
@EnableJpaRepositories(basePackages = "triplestar.mixchat.domain") // JPA 리포지토리 경로
@EnableMongoRepositories(basePackages = "triplestar.mixchat.domain.chat.chat.repository") // MongoDB 리포지토리 경로
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
