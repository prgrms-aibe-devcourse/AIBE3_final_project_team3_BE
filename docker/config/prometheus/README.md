# Prometheus + Grafana 모니터링 가이드

## 🚀 시작하기

### 1. Docker Compose로 실행
```bash
# 프로젝트 루트에서
docker-compose up -d prometheus grafana

# 로그 확인
docker-compose logs -f prometheus grafana
```

### 2. 접속 정보
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3001
  - Username: `admin`
  - Password: `admin`

### 3. Spring Boot 애플리케이션 실행
```bash
# Gradle 실행
./gradlew bootRun

# 또는 IDE에서 실행
```

### 4. Actuator 엔드포인트 확인
```bash
# Health Check
curl http://localhost:8080/actuator/health

# Prometheus 메트릭
curl http://localhost:8080/actuator/prometheus

# 모든 엔드포인트 목록
curl http://localhost:8080/actuator
```

## 📊 Grafana 대시보드 설정

### 방법 1: 공식 대시보드 Import (추천)

1. Grafana에 로그인 (http://localhost:3001)
2. 왼쪽 메뉴 > Dashboards > Import
3. 다음 대시보드 ID 입력:

**Spring Boot 대시보드:**
- **11378** - JVM (Micrometer) - 메모리, GC, 스레드
- **4701** - Spring Boot Statistics - HTTP 요청, 응답시간
- **12900** - Spring Boot 2.1+ System Monitor

**기타 유용한 대시보드:**
- **3662** - Prometheus 2.0 Stats
- **1860** - Node Exporter Full (서버 모니터링 시)

4. Prometheus 데이터소스 선택
5. Import 클릭

### 방법 2: 커스텀 대시보드 생성

#### 채팅 도메인 핵심 메트릭

```promql
# HTTP 요청 속도 (RPS)
rate(http_server_requests_seconds_count{uri=~"/api/v1/chats.*"}[1m])

# 응답 시간 (p95)
histogram_quantile(0.95,
  rate(http_server_requests_seconds_bucket{uri=~"/api/v1/chats.*"}[1m]))

# 에러율
rate(http_server_requests_seconds_count{uri=~"/api/v1/chats.*",status=~"5.."}[1m])
/
rate(http_server_requests_seconds_count{uri=~"/api/v1/chats.*"}[1m])

# JVM 메모리 사용량
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100

# DB Connection Pool 사용률
hikaricp_connections_active / hikaricp_connections_max * 100

# GC 시간
rate(jvm_gc_pause_seconds_sum[1m])
```

## 🎯 부하 테스트 시 모니터링

### 핵심 관찰 지표

1. **HTTP 응답 시간 (Response Time)**
   - p50, p95, p99 확인
   - 부하 증가에 따른 변화 관찰

2. **처리량 (Throughput)**
   - RPS (Requests Per Second)
   - 시스템 한계 파악

3. **에러율 (Error Rate)**
   - 4xx, 5xx 에러 비율
   - 임계점에서 급증하는지 확인

4. **JVM 메트릭**
   - Heap Memory 사용량
   - GC 빈도 및 시간
   - 스레드 수

5. **DB Connection Pool**
   - Active Connections
   - Pending Connections
   - Connection Timeout

6. **시스템 리소스**
   - CPU 사용률
   - 메모리 사용률

## 🔍 PromQL 쿼리 예제

### 채팅 메시지 조회 API 성능
```promql
# 메시지 조회 API p95 응답시간
histogram_quantile(0.95,
  sum(rate(http_server_requests_seconds_bucket{
    uri="/api/v1/chats/rooms/{roomId}/messages"
  }[1m])) by (le))

# 메시지 조회 API 초당 요청 수
sum(rate(http_server_requests_seconds_count{
  uri="/api/v1/chats/rooms/{roomId}/messages"
}[1m]))
```

### 채팅방 목록 조회 성능
```promql
# 그룹 채팅방 목록 조회 p95
histogram_quantile(0.95,
  sum(rate(http_server_requests_seconds_bucket{
    uri="/api/v1/chats/rooms/group"
  }[1m])) by (le))
```

### DB 쿼리 성능 (N+1 문제 탐지)
```promql
# 쿼리 실행 시간이 긴 경우
histogram_quantile(0.95,
  sum(rate(spring_data_repository_invocations_seconds_bucket[1m])) by (le, method))
```

## 📈 알람 설정 (Grafana Alerting)

### 예제: 응답 시간 초과 알람
```yaml
# Grafana에서 Alert Rule 생성
조건: p95 응답시간 > 1초
지속시간: 5분 이상
알림 채널: Slack, Email 등
```

### 예제: 에러율 급증 알람
```yaml
조건: 에러율 > 1%
지속시간: 2분 이상
```

### 예제: DB Connection Pool 고갈
```yaml
조건: Active Connections / Max Connections > 90%
지속시간: 3분 이상
```

## 🛠 트러블슈팅

### Prometheus가 메트릭을 수집하지 못함
1. Spring Boot 앱이 실행 중인지 확인
2. http://localhost:8080/actuator/prometheus 접속 확인
3. Prometheus targets 확인: http://localhost:9090/targets
4. Docker 네트워크 설정 확인

### Grafana에서 데이터가 보이지 않음
1. Prometheus 데이터소스 연결 확인
2. PromQL 쿼리 문법 확인
3. 시간 범위 설정 확인 (Last 5 minutes 등)

## 📝 부하 테스트 전 체크리스트

- [ ] Prometheus 실행 확인
- [ ] Grafana 실행 및 대시보드 설정
- [ ] Spring Boot Actuator 메트릭 노출 확인
- [ ] 주요 메트릭 쿼리 테스트
- [ ] 알람 설정 (선택)
- [ ] Before 상태 스냅샷 캡처
