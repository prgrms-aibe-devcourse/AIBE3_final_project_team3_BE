package triplestar.mixchat.global.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.lang.NonNull;

// API 응답 포맷
@Schema(description = "공통 응답 포맷")
public record CustomResponse<T>(
        @JsonIgnore int statusCode,
        @NonNull String msg,
        @Schema(description = "응답 데이터")
        T data
) {
    private static final List<Integer> ALLOWED_STATUS_CODES = List.of(
            200,
            400, 401, 403, 404, 405, 429,
            500
    );

    // 모든 생성 경로에서 검증
    public CustomResponse {
        validateStatusCode(statusCode);
    }

    public CustomResponse(int statusCode, String msg) {
        this(statusCode, msg, null);
    }

    public static <T> CustomResponse<T> ok(String msg, T data) {
        return new CustomResponse<>(200, msg, data);
    }

    public static CustomResponse<Void> ok(String msg) {
        return new CustomResponse<>(200, msg);
    }

    private void validateStatusCode(int statusCode) {
        if (!ALLOWED_STATUS_CODES.contains(statusCode)) {
            throw new RuntimeException("허용되지 않은 상태코드 : " + statusCode);
        }
    }
}
