package triplestar.mixchat.domain.chat.exception;

import org.springframework.http.HttpStatus;
import triplestar.mixchat.global.customException.ServiceException;

public class TooManyRequestsException extends ServiceException {
    public TooManyRequestsException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS.value(), message);
    }

    public TooManyRequestsException() {
        this("API 호출 횟수 제한을 초과했습니다. 잠시 후 다시 시도해주세요.");
    }
}
