package triplestar.mixchat.global.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile multipartFile, String dirName) {
        // 파일 이름 생성 (중복 방지)
        String fileName = createFileName(multipartFile.getOriginalFilename(), dirName);

        try (InputStream inputStream = multipartFile.getInputStream()) {
            // S3에 파일 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(multipartFile.getContentType())
                    .acl(ObjectCannedACL.PUBLIC_READ) // Public Read 권한 부여
                    .build();

            RequestBody requestBody = RequestBody.fromInputStream(inputStream, multipartFile.getSize());

            s3Client.putObject(putObjectRequest, requestBody);

            // 업로드된 파일의 URL 반환
            URL fileUrl = s3Client.utilities().getUrl(GetUrlRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build());
            return fileUrl.toString();

        } catch (IOException e) {
            log.error("S3 파일 업로드 실패: {}", e.getMessage());
            throw new RuntimeException("S3 파일 업로드 실패", e);
        }
    }

    private String createFileName(String originalFileName, String dirName) {
        return dirName + "/" + UUID.randomUUID().toString() + getFileExtension(originalFileName);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("파일 이름이 비어있습니다.");
        }
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("잘못된 형식의 파일입니다: " + fileName);
        }
    }

    public void deleteFileByUrl(String profileImageUrl) {
        s3Client.deleteObject(builder -> builder.bucket(bucket)
                .key((profileImageUrl))
                .build());
    }
}
