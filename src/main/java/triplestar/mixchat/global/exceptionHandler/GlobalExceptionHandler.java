package triplestar.mixchat.global.exceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import triplestar.mixchat.global.response.ApiResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private void commonLog(Exception e) {
        log.error("{} : {}", e.getClass().getSimpleName(), e.getMessage(), e);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handle(EntityNotFoundException e) {
        commonLog(e);

        return new ResponseEntity<>(
                new ApiResponse<>(
                        NOT_FOUND.value(),
                        "존재하지 않는 엔티티에 접근했습니다."
                ),
                NOT_FOUND
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handle(IllegalArgumentException e) {
        commonLog(e);

        return new ResponseEntity<>(
                new ApiResponse<>(
                        BAD_REQUEST.value(),
                        "잘못된 요청입니다."
                ),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handle(MethodArgumentNotValidException e) {
        commonLog(e);

        return new ResponseEntity<>(
                new ApiResponse<>(
                        BAD_REQUEST.value(),
                        "요청 값이 유효하지 않습니다."
                ),
                BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        commonLog(e);

        return new ResponseEntity<>(
                new ApiResponse<>(
                        BAD_REQUEST.value(),
                        "요청 본문 형식이 잘못되었거나 필수 값이 누락되었습니다."
                ),
                BAD_REQUEST
        );
    }
}