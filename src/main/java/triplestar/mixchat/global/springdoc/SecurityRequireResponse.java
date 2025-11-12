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
 * 401, 403 에러 응답을 자동으로 포함시킵니다.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "403",
                description = "접근 권한 없음 (FORBIDDEN) - 인증은 성공했으나 해당 리소스 접근 권한이 없음",
                content = @Content(
                        schema = @Schema(implementation = triplestar.mixchat.global.response.ApiResponse.class),
                        examples = @ExampleObject(
                                summary = "접근 권한 부족 예시",
                                value = "{\"msg\": \"해당 리소스에 접근할 권한이 없습니다.\"}"
                        )
                )
        )
})
@SignInInRequireResponse
public @interface SecurityRequireResponse {
}