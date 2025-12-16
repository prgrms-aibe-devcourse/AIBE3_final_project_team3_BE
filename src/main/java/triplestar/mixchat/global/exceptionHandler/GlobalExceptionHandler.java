package triplestar.mixchat.global.exceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import jakarta.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import triplestar.mixchat.global.customException.ServiceException;
import triplestar.mixchat.global.response.CustomResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active}")
    private String activeProfile;

    private void commonExceptionLog(Exception e) {
        log.warn("[ExceptionHandler] {} : {}", e.getClass().getSimpleName(), e.getMessage(), e);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<CustomResponse<Void>> handle(EntityNotFoundException e) {
        commonExceptionLog(e);

        return new ResponseEntity<>(
                new CustomResponse<>(
                        NOT_FOUND.value(),
                        "존재하지 않는 엔티티에 접근했습니다."
                ),
                NOT_FOUND
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomResponse<Void>> handle(IllegalArgumentException e) {
        commonExceptionLog(e);

        return new ResponseEntity<>(
                new CustomResponse<>(
                        BAD_REQUEST.value(),
                        "잘못된 요청입니다."
                ),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<CustomResponse<Void>> handle(IllegalStateException e) {
        commonExceptionLog(e);

        return new ResponseEntity<>(
                new CustomResponse<>(
                        BAD_REQUEST.value(),
                        "해당 요청을 처리할 수 없는 상태입니다."
                ),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomResponse<Void>> handle(MethodArgumentNotValidException e) {
        commonExceptionLog(e);

        return new ResponseEntity<>(
                new CustomResponse<>(
                        BAD_REQUEST.value(),
                        "요청 값이 유효하지 않습니다."
                ),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomResponse<Void>> handle(HttpMessageNotReadableException e) {
        commonExceptionLog(e);

        return new ResponseEntity<>(
                new CustomResponse<>(
                        BAD_REQUEST.value(),
                        "요청 본문 형식이 잘못되었거나 필수 값이 누락되었습니다."
                ),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<CustomResponse<Void>> handle(ServiceException e) {
        commonExceptionLog(e);

        return new ResponseEntity<>(
                new CustomResponse<>(
                        e.getStatusCode(),
                        e.getMessage()
                ),
                HttpStatus.valueOf(e.getStatusCode())
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CustomResponse<Void>> handle(AuthenticationException e) {
        commonExceptionLog(e);

        return new ResponseEntity<>(
                new CustomResponse<>(
                        UNAUTHORIZED.value(),
                        "사용자 인증에 실패했습니다."
                ),
                UNAUTHORIZED
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CustomResponse<Void>> handle(AccessDeniedException e) {
        commonExceptionLog(e);

        return new ResponseEntity<>(
                new CustomResponse<>(
                        FORBIDDEN.value(),
                        "접근 권한이 없습니다."
                ),
                FORBIDDEN
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponse<Void>> handle(Exception e) throws Exception {
        log.error("[ExceptionHandler] {} : {}", e.getClass().getSimpleName(), e.getMessage(), e);

        // prod 환경이 아니면 예외를 숨기지 않고 그대로 던짐
        if (!"prod".equals(activeProfile)) {
            throw e;
        }

        return new ResponseEntity<>(
                new CustomResponse<>(
                        INTERNAL_SERVER_ERROR.value(),
                        "서버에서 알 수 없는 오류가 발생했습니다."
                ),
                INTERNAL_SERVER_ERROR
        );
    }
}