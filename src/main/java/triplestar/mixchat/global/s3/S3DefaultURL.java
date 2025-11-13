package triplestar.mixchat.global.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3DefaultURL {

    @Value("${cloud.aws.s3.endpoint}")
    private String endpoint;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${member.default-image-name}")
    private String defaultProfileImageName;

    @Bean
    public String defaultProfileBaseURL() {
        // '/'로 끝나는 endpoint 처리
        String cleanEndpoint = endpoint.replaceAll("/$", "");

        return cleanEndpoint + "/" + bucket + "/" + defaultProfileImageName;
    }
}
