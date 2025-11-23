# 첫 번째 스테이지: 빌드 스테이지
FROM gradle:jdk-21-and-23-graal-jammy AS builder

# 작업 디렉토리 설정
WORKDIR /app

# 소스 코드와 Gradle 래퍼 복사
COPY build.gradle.kts .
COPY settings.gradle.kts .

# 종속성 설치
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY .env .
COPY .env.prod.properties .
COPY .env.staging.properties .
COPY src src

# 애플리케이션 빌드
RUN gradle build --no-daemon

# 두 번째 스테이지: 실행 스테이지
FROM container-registry.oracle.com/graalvm/jdk:21

# 작업 디렉토리 설정
WORKDIR /app

# 첫 번째 스테이지에서 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar
COPY --from=builder /app/.env .env
COPY --from=builder /app/.env.prod.properties .env.prod.properties
COPY --from=builder /app/.env.staging.properties .env.staging.properties

# 실행할 JAR 파일 지정 (스테이징 서버에서는 변경 필요)
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
