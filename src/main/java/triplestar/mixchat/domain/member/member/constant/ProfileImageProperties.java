package triplestar.mixchat.domain.member.member.constant;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProfileImageProperties {

    @Value("${cloud.aws.s3.endpoint}")
    private String endpoint;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${member.default-image-name}")
    private String defaultProfileImageName;

    @Value("${member.max-image-size-bytes}")
    private Long maxProfileImageSizeBytes;

    @Value("${member.allowed-image-types}")
    private String allowedImageTypes;

    public String defaultProfileImageUrl() {
        // '/'로 끝나는 endpoint 처리
        String cleanEndpoint = endpoint.replaceAll("/$", "");

        return cleanEndpoint + "/" + bucket + "/" + defaultProfileImageName;
    }

    public Long maxProfileImageSizeBytes() {
        return maxProfileImageSizeBytes;
    }

    public Set<String> allowedImageTypes() {
        return Arrays.stream(allowedImageTypes.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
    }
}
