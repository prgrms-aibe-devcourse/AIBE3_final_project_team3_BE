# MixChat - 실시간 AI 번역 채팅 & 커뮤니티
<img src="./docs/images/mixchat-logo.png" width="500" />

> MixChat은 실시간 채팅에 AI 번역·피드백, 커뮤니티 게시판, 학습 노트를 결합한 영어 학습/언어 교환 서비스입니다. 대화 맥락과 사용자 노트를 RAG로 묶어 개인화된 도움을 주고, 커뮤니티/친구/알림까지 한 번에 제공합니다.

- 서비스: [mixchat](https://mixchat.site)
- API Docs: [Swagger UI](http://localhost:8080/swagger-ui/index.html)

---

## 목차
- [프로젝트 소개](#프로젝트-소개)
- [주요 기능](#주요-기능)
- [도메인 별 상세](#도메인-별-상세)
- [아키텍처](#아키텍처)
- [기술 스택](#기술-스택)
- [실행 방법](#실행-방법)
- [환경 변수](#환경-변수)
- [폴더 구조](#폴더-구조)
- [성능·운영 개선 포인트](#성능·운영-개선-포인트)
- [앞으로 추가하면 좋은 것](#앞으로-추가하면-좋은-것)

---

## 프로젝트 소개
MixChat은 사용자 간의 실시간 채팅과 AI 기반의 다국어 번역 기능을 제공하는 서비스입니다.    
언어 장벽을 낮추는 **실시간 번역**과, 대화/피드백을 축적하는 **학습 노트**를 기반으로 한 **개인화 AI 튜터(RAG)** 를 통해 글로벌 커뮤니케이션과 외국어 학습을 돕습니다.

---

## 주요 기능
### 💬 실시간 채팅
- 1:1 개인 채팅, 그룹 채팅, AI 튜터 챗봇과의 대화
- WebSocket + STOMP 기반 실시간 메시지 전송(텍스트, 파일)
- 메시지/채팅방별 읽음 상태 및 미읽음 수 실시간 동기화

### 🌐 실시간 AI 번역, 피드백
- Ollama·OpenAI·Gemini를 선택적으로 사용해 실시간 번역(안정성 확보)
- 실시간 번역, 문장 교정, 롤플레이 챗봇에 적절한 모델 사용으로 토큰 비용 최적화

### 🧠 AI 튜터 챗봇 (RAG)
- 사용자의 학습 노트를 활용한 개인화 컨텍스트 제공
- SQL 기반 컨텍스트 검색 + LLM 응답 생성 (Retrieval Augmented Generation)

### 🔔 소셜 & 알림
- 온라인 상태(Presence) 표시
- 친구 요청/수락/차단, 채팅/게시판 활동 알림

### 📰 커뮤니티 게시판
- 게시글 작성/수정/삭제, 이미지 업로드(S3/MinIO)
- 댓글·좋아요, 조회수·인기/최신 정렬 지원

### 🔐 로그인 및 권한 관리
- JWT 기반 인증/인가
- Spring Security 기반 API 보호
- 관리자가 신고 접수 및 처리, 방 강제 종료·게시글 제거

### 🔎 데이터 검색(Elasticsearch)
- AI 튜터와의 대화, 사용자 학습 노트를 ES에 저장 및 관리
- AI 튜터 답변 시, ES에 저장된 학습 노트에서 관련 지식 검색 및 제공 (RAG)
- ES 기반 채팅 메시지 본문 검색(Prefix 지원)

### 📊 관측성·운영 도구
- Spring Actuator + Micrometer → Prometheus → Grafana 대시보드
- 테스트 컨테이너 기반 통합 테스트

---

## 아키텍처

---

## 기술 스택
**Language & Framework**  
- Java 21, Spring Boot 3, Spring Web, WebSocket(STOMP), Spring Security (JWT), Spring Data JPA, QueryDSL

**Data**
- MySQL, Redis, MongoDB, Elasticsearch

**AI**
- Spring AI, OpenAI, Ollama (Local LLM), Gemini, RAG, Embedding

**Infra**  
- Docker, Docker Compose, RabbitMQ, AWS S3(로컬 MinIO)

**Observability**  
- Spring Actuator, Micrometer → Prometheus → Grafana

**Build & Tooling**
- Gradle, Testcontainers, Swagger (OpenAPI)

---

## 폴더 구조
```
src/main/java/triplestar/mixchat
├── domain
│   ├── admin
│   ├── ai
│   │   ├── chatbot
│   │   ├── rag
│   │   ├── systemprompt
│   │   └── userprompt
│   ├── chat
│   │   ├── chat
│   │   └── search
│   ├── learningNote
│   ├── member
│   ├── miniGame
│   ├── notification
│   ├── post
│   └── report
└── global
    ├── ai
    ├── cache
    ├── config
    ├── exception
    ├── response
    ├── security
    └── s3
```

---

## 성능·운영 개선 포인트
- **Event-Driven 후처리**: `@TransactionalEventListener(AFTER_COMMIT)+@Async`로 번역/인덱싱/알림을 분리, API 응답 지연 감소.

---
