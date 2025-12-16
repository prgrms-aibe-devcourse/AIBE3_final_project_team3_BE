package triplestar.mixchat.global.springdoc;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 모든 API 엔드포인트의 성공 응답(200 OK) 스키마를 정의합니다.
 * ApiResponse<Void>인 경우 일반적인 성공 메시지를 자동으로 포함시킵니다.
 * 응답 반환값이 있는 경우 해당 타입에 맞는 스키마가 자동으로 적용됩니다.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
        responseCode = "200",
        description = "요청 처리 성공"
)
public @interface SuccessResponse {
}
