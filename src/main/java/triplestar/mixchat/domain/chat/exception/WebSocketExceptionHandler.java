package triplestar.mixchat.domain.chat.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import triplestar.mixchat.global.customException.ServiceException;

// todo: 추후 작동 확인 필요
@Slf4j
@RestControllerAdvice
public class WebSocketExceptionHandler {

    /**
     * 예측 가능한 비즈니스 로직 예외 처리 (e.g., 채팅방 멤버가 아님)
     * 로그 레벨: WARN
     */
    @MessageExceptionHandler(ServiceException.class)
    @SendToUser("/topic/errors")
    public String handleServiceException(ServiceException ex) {
        log.warn("[WebSocket] ServiceException: status={}, message={}", ex.getStatusCode(), ex.getMessage());
        return ex.getMessage(); // 서비스 예외에 담긴 구체적인 메시지를 사용자에게 전달
    }

    /**
     * 400 Bad Request: 잘못된 인자나 요청 형식 오류 처리
     * 로그 레벨: WARN
     */
    @MessageExceptionHandler(IllegalArgumentException.class)
    @SendToUser("/topic/errors")
    public String handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("[WebSocket] IllegalArgumentException: {}", ex.getMessage());
        return "잘못된 요청입니다: " + ex.getMessage();
    }

    /**
     * 인가/권한 관련 예외 처리
     * 로그 레벨: WARN
     */
    @MessageExceptionHandler(AccessDeniedException.class)
    @SendToUser("/topic/errors")
    public String handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("[WebSocket] AccessDeniedException: {}", ex.getMessage());
        return "요청에 대한 접근 권한이 없습니다.";
    }

    /**
     * 401 Unauthorized: 인증 관련 예외 처리 (e.g., 잘못된 토큰)
     * 로그 레벨: WARN
     */
    @MessageExceptionHandler(AuthenticationException.class)
    @SendToUser("/topic/errors")
    public String handleAuthenticationException(AuthenticationException ex) {
        log.warn("[WebSocket] AuthenticationException: {}", ex.getMessage());
        return "인증에 실패했습니다. " + ex.getMessage();
    }

    /**
     * 예측하지 못한 모든 서버 예외 처리
     * 로그 레벨: ERROR
     */
    @MessageExceptionHandler(Exception.class)
    @SendToUser("/topic/errors")
    public String handleException(Exception ex) {
        log.error("[WebSocket] Unexpected Exception:", ex);
        return "메시지 처리 중 예상치 못한 오류가 발생했습니다.";
    }
}
