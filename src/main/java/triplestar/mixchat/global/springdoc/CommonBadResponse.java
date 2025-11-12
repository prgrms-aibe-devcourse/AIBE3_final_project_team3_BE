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
 * 대부분의 API 엔드포인트에서 발생 가능한 일반적인 클라이언트 오류 응답 스키마를 정의합니다.
 * 400 Bad Request와 404 Not Found 응답을 자동으로 포함시킵니다.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (BAD_REQUEST) - 유효성 검증 실패 또는 필수 값 누락 등",
                content = @Content(
                        schema = @Schema(implementation = ApiResponse.class),
                        examples = @ExampleObject(
                                name = "400_BadRequest",
                                summary = "잘못된 요청 예시",
                                value = "{\"msg\": \"요청 본문의 형식이 잘못되었거나 필수 값이 누락되었습니다.\"}"
                        )
                )
        ),
        @ApiResponse(
                responseCode = "404",
                description = "리소스를 찾을 수 없음 (NOT_FOUND) - 존재하지 않는 경로 또는 일반적인 리소스 미발견",
                content = @Content(
                        schema = @Schema(implementation = ApiResponse.class),
                        examples = @ExampleObject(
                                name = "404_NotFound",
                                summary = "일반적인 리소스 미발견 예시",
                                value = "{\"msg\": \"요청하신 경로를 찾을 수 없습니다.\"}"
                        )
                )
        )
})
public @interface CommonBadResponse {
}