# CI/CD Workflow 가이드

## 📋 워크플로우 개요

이 프로젝트는 3개의 독립적인 워크플로우로 구성되어 있습니다:

1. **Build**: Docker 이미지 빌드 및 GHCR 푸시
2. **CI**: 통합 테스트 실행
3. **CD**: 자동 배포 (Staging/Production)

## 🔄 워크플로우 실행 흐름

### Dev 브랜치 (Staging)
```
PR 생성 → Build → CI (테스트)
     ↓
  Merge
     ↓
Build → CI → CD (Staging 배포)
```

### Prod 브랜치 (Production)
```
PR 생성 → Build → CI (테스트)
     ↓
  Merge
     ↓
Build → CI → CD-Prod (Production 배포)
```

## 🚀 시작하기

### 1. GitHub Secrets 설정

Repository Settings → Secrets and variables → Actions 에서 다음 시크릿을 등록하세요:

#### 필수 시크릿 (Staging)
```
STAGING_SSH_PRIVATE_KEY=<SSH 개인키>
STAGING_SERVER_HOST=staging.example.com
STAGING_SERVER_USER=ubuntu
```

#### 필수 시크릿 (Production)
```
PROD_SSH_PRIVATE_KEY=<SSH 개인키>
PROD_SERVER_HOST=prod.example.com
PROD_SERVER_USER=ubuntu
```

#### 선택 시크릿 (기본값 제공됨)
```
MYSQL_ROOT_PASSWORD=rootpw
MYSQL_TEST_DATABASE=mysql_test
MYSQL_TEST_USER=testuser
MYSQL_TEST_PASSWORD=testpass
REDIS_PASSWORD=redispass
MONGO_INITDB_ROOT_USERNAME=mongoadmin
MONGO_INITDB_ROOT_PASSWORD=mongopass
MONGO_TEST_DB=mongo_test
```

### 2. SSH 키 생성

#### Staging 서버용
```bash
ssh-keygen -t rsa -b 4096 -f ~/.ssh/github_actions_staging -C "github-actions-staging"
cat ~/.ssh/github_actions_staging  # 이 내용을 STAGING_SSH_PRIVATE_KEY에 복사
```

#### Production 서버용
```bash
ssh-keygen -t rsa -b 4096 -f ~/.ssh/github_actions_prod -C "github-actions-prod"
cat ~/.ssh/github_actions_prod  # 이 내용을 PROD_SSH_PRIVATE_KEY에 복사
```

#### 서버에 공개키 등록
```bash
# Staging 서버
cat ~/.ssh/github_actions_staging.pub >> ~/.ssh/authorized_keys

# Production 서버
cat ~/.ssh/github_actions_prod.pub >> ~/.ssh/authorized_keys
```

### 3. 서버 준비

각 서버에 다음 디렉토리 구조를 생성하세요:

```bash
# Staging 서버
mkdir -p /opt/app/staging
cd /opt/app/staging
# docker-compose.yml 파일 배치

# Production 서버
mkdir -p /opt/app/production
cd /opt/app/production
# docker-compose.yml 파일 배치
```

### 4. GitHub Container Registry 권한 설정

저장소의 Packages 설정에서:
1. Package settings → Manage Actions access
2. `Add Repository` 클릭
3. 해당 저장소에 `Write` 권한 부여

## 📝 워크플로우 상세 설명

### Build Workflow (`build.yml`)

**트리거:**
- `dev` 또는 `prod` 브랜치로 PR 생성
- `dev` 또는 `prod` 브랜치로 Push

**동작:**
1. Dockerfile 기반 이미지 빌드
2. GitHub Container Registry(GHCR)에 푸시
3. 태그:
   - PR: `pr-{PR번호}`
   - Commit: `{SHA}`
   - Dev: `dev-latest`
   - Prod: `prod-latest`

**권한:** `packages: write` (GHCR 푸시용)

### CI Workflow (`ci.yml`)

**트리거:**
- `dev` 또는 `prod` 브랜치로 PR 생성
- `dev` 또는 `prod` 브랜치로 Push

**동작:**
1. Build 워크플로우 완료 대기
2. MySQL, Redis, MongoDB 서비스 컨테이너 시작
3. 서비스 준비 상태 확인
4. Gradle 통합 테스트 실행

**서비스:**
- MySQL 8.0 (포트 3306)
- Redis 8.0 Alpine (포트 6379)
- MongoDB 6.0 (포트 27017)

**주의사항:**
- 서비스 컨테이너는 `127.0.0.1`로 접근
- Health check로 준비 상태 확인
- Gradle 캐시 활용으로 빌드 속도 향상

### CD Workflow - Staging (`cd.yml`)

**트리거:**
- `dev` 브랜치로 Push

**동작:**
1. CI 워크플로우 완료 대기
2. SSH로 Staging 서버 접속
3. `dev-latest` 이미지 pull
4. docker-compose로 재시작
5. 배포 검증

**환경:** `staging` (GitHub Environment)

**서버 경로:** `/opt/app/staging`

### CD Workflow - Production (`cd-prod.yml`)

**트리거:**
- `prod` 브랜치로 Push

**동작:**
1. CI 워크플로우 완료 대기
2. SSH로 Production 서버 접속
3. `prod-latest` 이미지 pull
4. docker-compose로 재시작
5. 배포 검증
6. 성공/실패 알림

**환경:** `production` (GitHub Environment)

**서버 경로:** `/opt/app/production`

## 🔍 트러블슈팅

### 문제 1: "Wait for build workflow" 타임아웃

**원인:** Build 워크플로우가 실패하거나 실행되지 않음

**해결:**
1. Actions 탭에서 Build 워크플로우 상태 확인
2. Dockerfile 구문 오류 확인
3. GHCR 권한 확인

### 문제 2: CI 테스트 실패

**원인:** 서비스 컨테이너 연결 실패

**해결:**
1. Health check 로그 확인
2. 환경변수 설정 확인 (`application-test.yml`)
3. 포트 충돌 확인

### 문제 3: SSH 연결 실패

**원인:** SSH 키 또는 서버 설정 문제

**해결:**
1. SSH 키 형식 확인 (개행 문자 포함)
2. `authorized_keys` 권한 확인 (`chmod 600`)
3. 서버 방화벽 확인
4. SSH 키가 정확한지 로컬에서 테스트:
   ```bash
   ssh -i ~/.ssh/github_actions_staging user@host
   ```

### 문제 4: Docker pull 실패

**원인:** GHCR 인증 문제

**해결:**
1. 서버에서 GHCR 로그인 확인
2. GITHUB_TOKEN 권한 확인
3. 패키지 접근 권한 확인

### 문제 5: "tr -d '\r'" 오류

**원인:** Windows 환경에서 생성된 SSH 키

**해결:**
- SSH 키를 복사할 때 개행 문자 제거 필수
- `tr -d '\r'` 명령이 이를 자동으로 처리

## 📊 모니터링

### GitHub Actions 로그 확인
1. Repository → Actions 탭
2. 워크플로우 선택
3. 각 Job의 로그 확인

### 서버 로그 확인
```bash
# Staging
ssh user@staging-server
cd /opt/app/staging
docker compose logs -f

# Production
ssh user@prod-server
cd /opt/app/production
docker compose logs -f
```

## 🔐 보안 권장사항

1. **SSH 키 관리**
   - 각 환경별 별도의 SSH 키 사용
   - 정기적으로 키 교체
   - 키는 반드시 GitHub Secrets에만 저장

2. **Secrets 관리**
   - 프로덕션 시크릿은 절대 코드에 포함하지 않기
   - 필요시 GitHub Environment Secrets 사용
   - 정기적으로 비밀번호 교체

3. **Environment Protection**
   - Production 환경에 Approval 규칙 설정
   - 배포 전 리뷰 프로세스 적용

## 🎯 Best Practices

1. **브랜치 전략**
   - `dev`: 개발 및 스테이징 배포
   - `prod`: 프로덕션 배포
   - Feature 브랜치 → dev PR → prod PR

2. **배포 전략**
   - Staging에서 충분한 테스트 후 Production 배포
   - Production 배포는 GitHub Environment Protection 활용

3. **롤백 전략**
   ```bash
   # 이전 이미지로 롤백
   docker tag ghcr.io/repo:prod-latest ghcr.io/repo:backup
   docker pull ghcr.io/repo:sha-abc1234
   docker tag ghcr.io/repo:sha-abc1234 ghcr.io/repo:prod-latest
   docker compose up -d
   ```

## 📚 추가 리소스

- [GitHub Actions 문서](https://docs.github.com/en/actions)
- [Docker Compose 문서](https://docs.docker.com/compose/)
- [GHCR 문서](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)

