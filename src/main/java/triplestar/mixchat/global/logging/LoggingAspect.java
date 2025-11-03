package triplestar.mixchat.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // 서비스 레이어 public 메서드 실행 전후 로그 기록
    @Around("execution(public * triplestar.mixchat..service..*(..))")
    public Object logServiceLayer(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("[Service] {}.{} 실행 - 파라미터: {}", className, methodName, args);

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;

        log.info("[Service] {}.{} 성공 - 실행시간: {}ms", className, methodName, executionTime);

        return result;
    }
}