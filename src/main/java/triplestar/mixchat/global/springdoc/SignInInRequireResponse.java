package triplestar.mixchat.global.springdoc;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 인증(Authentication)이 필요한 API 엔드포인트에 공통적으로 적용될 응답 스키마를 정의합니다.
 * 401 에러 응답을 자동으로 포함시킵니다.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(
        responseCode = "401",
        description = "인증 실패 (UNAUTHORIZED) - 유효하지 않은 토큰 또는 인증 정보 누락",
        content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                        name = "401_Unauthorized",
                        summary = "인증 실패 예시",
                        value = "{\"msg\": \"인증 정보가 유효하지 않거나 누락되었습니다.\"}"
                )
        )
)
public @interface SignInInRequireResponse {
}