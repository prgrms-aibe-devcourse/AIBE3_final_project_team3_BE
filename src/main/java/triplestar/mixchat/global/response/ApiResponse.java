package triplestar.mixchat.global.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import org.springframework.lang.NonNull;

// API 응답 포맷
public record ApiResponse<T>(
        @JsonIgnore int statusCode,
        @NonNull String msg,
        T data
) {
    private static final List<Integer> ALLOWED_STATUS_CODES = List.of(
            200,
            400, 401, 403, 404, 405, 429,
            500
    );

    // 모든 생성 경로에서 검증
    public ApiResponse {
        validateStatusCode(statusCode);
    }

    public ApiResponse(int statusCode, String msg) {
        this(statusCode, msg, null);
    }

    public static <T> ApiResponse<T> ok(String msg, T data) {
        return new ApiResponse<>(200, msg, data);
    }

    public static ApiResponse<Void> ok(String msg) {
        return new ApiResponse<>(200, msg);
    }

    private void validateStatusCode(int statusCode) {
        if (!ALLOWED_STATUS_CODES.contains(statusCode)) {
            throw new RuntimeException("허용되지 않은 상태코드 : " + statusCode);
        }
    }
}
