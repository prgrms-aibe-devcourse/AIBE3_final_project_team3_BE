---

# 규칙 및 컨벤션

- 모든 규칙은 협의 후 수정이 가능함
    - 반대로 수정 전까지는 규칙 및 컨벤션을 준수하여 작업하고 특이사항 발생시 공유
- 수정이 필요한 부분이 발생할 경우 적극적으로 의견 제시 가능

# 🏉 팀 규칙 및 협업 방식

## 1. 회의 및 커뮤니케이션

- **데일리 스크럼**
    - 공통 : 이슈 및 작업 트래커 확인을 통해 작업 현황 공유
    - 오전 9시 : 개발 시작 전, 각자 전날 작업 리뷰 및 컨디션/이슈 공유
    - 오후 5시 : 오늘 개발에 대해 간단한 리뷰(진행상황, 문서화)
- **주간 회의**
    - 매주 금요일 dev → main 병합 전 dev 브랜치 점검
- **멘토링**: 정해진 사항 없음

## 2. 협업 문화

- 1시간 고민한 문제는 팀에 바로 공유
- 요청이 있을 때 적극적으로 돕는 분위기 조성
- 다양한 의견이 자유롭게 오갈 수 있도록 상호 존중
- 정기 일정(회의/멘토링 등) 불참 시 사전 공유 필수
- 슬랙 커뮤니케이션 규칙:
    - 채팅 확인 시 👍 이모지
    - 이슈/작업 관련 내용은 슬랙에 기록
    - **6시 이후 PR을 올릴 경우 PR전용 스레드에 댓글을 남길 것**
        - 10분 이내에 담당 인원의 반응이 없을 경우 다른 인원에게 요청 가능

---

# 🧩 Git & 코드 협업 컨벤션

## 1. 커밋 컨벤션 (prefix)

| 태그 | 설명 |
| --- | --- |
| feat | 새로운 기능 추가 |
| fix | 버그 수정 |
| style | 코드 스타일 변경 |
| refactor | 리팩토링 |
| design | CSS 등 디자인 수정 |
| comment | 주석 추가/수정 |
| docs | 문서 수정 |
| test | 테스트 추가/수정 |
| chore | 설정/빌드 등 기타 변경 |
| rename | 파일명/폴더명 변경 |
| remove | 파일 삭제 |

> 예시: feat: 로그인 기능 구현
>

## 2. 코드 컨벤션

- 들여쓰기 등은 [기본적으로 우테코 코드포매터 사용](https://github.com/woowacourse/woowacourse-docs/blob/main/styleguide/java/intellij-java-wooteco-style.xml)하여 정리
- 클래스명: `PascalCase`
- 변수/함수: `camelCase`
- 경로명: `samllCase`
- DB 컬럼: `snake_case`
- 클래스명: `PascalCase`
- 변수/함수: `camelCase`
- 경로명: `samllCase`
- DB 컬럼: `snake_case`
- DTO
    - 컨트롤러 ↔ 서비스간의 DTO에는 `Req`, `Resp` 어미 사용
    - 그 외에는 기본적으로 `DTO` 어미 사용
    - ~~필수 입력에 대해서는 `@Notnull` 필수~~
- 주석: 꼭 필요한 경우만, 들여쓰기에 맞게 작성
- Builder패턴은 필요한 경우에 사용
- API 명세 도구: Swagger

(11/11 멘토링 이후 추가 사항 코드 통일성 강화를 위한 추가 컨벤션 → 협의 후 확정예정)

- DTO
    - 기본적으로 record 사용(req에는 record, resp에는 class사용도 고려해볼 수 있음)
    - 필드에 `@Notnull`, `@Nullable` 반드시 추가
    - swagger `@Schema` (description, example) 추가 (AI가 해줌)
- Entity
    - setter사용 금지 → domain 메소드를 이용하여 setter사용 피할 수 있음
    - NOT NULL 필드에 `@Column(nullable = false)` 추가, NULL 허용 필드에는 nullable = true 생략
    - 기본생성자 접근제어자 protected로 생성
    - BaseEntity 혹은 BaseEntityNoModified 상속
    - Builder패턴에 대해(논의 필요)
        - 클래스레벨에 사용 금지, 필수 필드는 nullCheck사용으로 생성자의 견고함 Builder의 유연성 챙길 수 있음
        - 예시

            ```java
            @Getter
            @NoArgsConstructor(access = AccessLevel.PROTECTED)
            public class Member {
                @Builder
                public Member(String email, String password, String nickname, String description) {
                    // 필수 필드가 null이면 예외를 던지는 검증 로직 추가 가능
                    if (email == null || password == null) {
                        throw new IllegalArgumentException("이메일과 비밀번호는 필수입니다.");
                    }
                    this.email = email;
                    this.password = password;
                    this.nickname = nickname;
                    this.description = description;
                }
            }
            ```

- Service(논의 필요)
    - Create 관련 service의 반환값은 void대신 Entity, dto혹은 컨트롤러로 반환할 값이 없다면 Id로
- Controller에서 Repository직접 참조로 Entity 생성 금지
    - service협력으로 얻은 Entity생성 반환값을 dto 전환하는 것은 가능
    - MemberRepository 참조 X → 서비스에서 MemberRepository참조(논의필요)
    - get요청 post요청은 반환값 O(dto 혹은 Id), 나머지는 반환값 X
- swagger 문서화
    - 특정 기능이 완성 상태에 카드를 놓기전에 ‘swagger’문서화를 모두 끝낼 것
        - dto 스키마
        - 컨트롤러 스키마
    - controller를 인터페이스를 통해 분리하여 swagger 관련 코드 분리


## 3. Git Flow & 브랜치

- 브랜치명은 feat/member 와 같이 태그/기능명으로 설정
- 브랜치는 이슈 단위로 생성
- 기능은 최대한 작게 쪼개서 이슈 & PR 생성

---

# 📝 이슈 & PR 작성 규칙

## 이슈 템플릿

**기능**

```
## #️⃣ 요청 유형

> 해당되는 항목을 선택해주세요.
- [ ] 새로운 기능 추가
- [ ] 기존 기능 개선
- [ ] 성능 개선

## #️⃣ 어떤 기능인가요?

> 추가하려는 기능 또는 개선하려는 부분에 대해 간결하게 설명해주세요.

## #️⃣ 예상 브랜치 이름

- 

## #️⃣ 이 기능이 필요한 이유는 무엇인가요?(선택)

> 이 기능이 왜 필요한지, 또는 개선/성능 향상이 필요한 이유에 대해 설명해주세요.

## #️⃣ 작업 상세 내용

- [ ] TODO

## 📎 참고할만한 자료 (선택)

> 관련 문서, 스크린샷, 또는 예시 등이 있다면 여기에 첨부해주세요.
```

**버그**

```json
## #️⃣ 어떤 버그인가요?

> #어떤 버그인지 간결하게 설명해주세요.

## #️⃣ 어떤 상황에서 발생한 버그인가요?

> (가능하면) Given-When-Then 형식으로 서술해주세요.

## #️⃣ 예상 결과

> 예상했던 정상적인 결과가 어떤 것이었는지 설명해주세요.

## #️⃣ 실제 결과

> 실제로 어떤 결과가 발생했는지 설명해주세요.

## 📎 참고할만한 자료(선택)

> 관련 문서, 스크린샷, 또는 예시 등이 있다면 여기에 첨부해주세요
```

## PR 템플릿

```
✨ Feat(브랜치명) : 제목

## 작성 전 체크리스트
- [ ] 로컬 테스트 완료
- [ ] 주석 최신화
- [ ] 민감정보 제외

## 연관 이슈
- Closes #이슈번호
- Related to #이슈번호

## 작업 내용
- 구현한 기능 1

## 주요 변경사항
- 추가: newFile.java
- 수정: loginService.java
- 삭제: oldHelper.js

## 병합 전 체크리스트
- [ ] 코드리뷰 피드백 반영 완료
- [ ] Notion 문서화 진행중/완료
```

## 바람직한 PR 주기

- PR 생성 타이밍: 기능 단위 완료 시, 하루 작업 마무리 시, 리뷰가 필요할 때
- PR 규모 예시:
    - 파일 수 5~10개 이하
    - 코드 200~500줄 이하
    - 리뷰 시간 30분 이내
    - 오전, 오후 1번 이상의 PR이 나올 수 있도록 작게 유지

## 머지 규칙

- 코드 리뷰 후 피드백 반영 및 문서화가 완료되면 작성자가 직접 머지
- 머지 후 브랜치 삭제

---

# 🔍 코드 리뷰 가이드

- 코드 리뷰는 김희수, 김찬종이 기본적으로 담당
    - 다른 개발자들도 얼마든지 리뷰 작성 가능
    - 김희수 → 게시글, 관리자 + 학습노트, 채팅방
    - 김찬종 → 회원, AI 채팅
- 작성자의 코드 의도를 파악하며 의견교환 지향
- 가능하다면 대안 및 학습 자료도 함께 제시

### 예시

❌ “이상함”

✅ “NPE가 발생할 수 있으니 Optional 사용을 고려해보는 것도 좋을 것 같습니다.”

---

# 🔐 보안 협업 수칙

## 인증/인가

- JWT + Spring Security 적용
- BCrypt로 비밀번호 암호화
- 관리자/사용자 권한 분리 처리

## 데이터 보호

- Native Query 최소화, JPA 권장
- 입력 검증: `@Valid`, `@NotNull` 등 사용
- 로그/에러 메시지에 민감정보 포함 금지

## 환경 보안

- 민감정보는 `.env` → GitHub Secrets로 관리
- `application.yml`에 하드코딩 금지
- HTTPS 환경에서 테스트/배포 진행
- 보안가이드 바로가기

  # 우선순위 적용 가이드

  ### ✅ **1단계 – 필수**

    - Spring Security + JWT
    - BCrypt 암호화
    - JPA 통한 SQL Injection 방지
    - 입력값 검증 (Bean Validation)
    - HTTPS 적용 (ALB/SSL)
    - GitHub Secrets 및 환경변수 보호
    - 민감정보 노출 방지

  ### ✅ **2단계 – 추천**

    - JWT 무효화 (로그아웃 블랙리스트)
    - CSRF/XSS 방어
    - 관리자/유저 권한 분리
    - 보안 헤더 적용 (Filter로)
    - ALB 보안 그룹 최소화
    - S3 presigned URL 적용
    - 로그 수집 및 감사 로그

  ### ✅ **3단계 – 고급/실무 반영**

    - 로그인 시도 제한 (Redis)
    - Rate Limiting
    - CI/CD 보안 (권한 최소화, 취약점 스캔)
    - 실시간 알림 (OneSignal)
    - WebSocket 인증
    - 소스 난독화, CSP 설정
    - Open Redirect 방지, Referrer 검증

    ---

  ## ✅ 포트폴리오 프로젝트 보안 구성 체크리스트

  | 구분 | 항목 | 설명 | 필수도 |
      | --- | --- | --- | --- |
  | 🔐 인증/인가 | JWT 인증 + Spring Security | 로그인/인가 처리 | ⭐️⭐️⭐️ |
  |  | 비밀번호 BCrypt 암호화 | 평문 저장 방지 | ⭐️⭐️⭐️ |
  |  | JWT 무효화 (로그아웃, 블랙리스트) | 탈취/세션 유지 방지 | ⭐️⭐️⭐ |
  |  | 로그인 시도 제한 (Brute Force 방지) | Redis 등으로 실패 횟수 제한 | ⭐️⭐ |
  |  | 권한별 접근 제한 (`/admin`, `ROLE_USER`) | URI + 기능 보호 | ⭐️⭐️⭐ |
  |  | WebSocket 인증 | 실시간 통신 보안 강화 | ⭐️⭐ |
  | 🔒 데이터 보호 | SQL Injection 방어 (JPA 사용) | PreparedStatement로 방어 | ⭐️⭐️⭐ |
  |  | XSS 방지 (입력값/출력값 처리) | `@SafeHtml`, escape 처리 등 | ⭐️⭐️⭐ |
  |  | CSRF 토큰 적용 | 스크립트 요청 위조 방지 | ⭐️⭐ |
  |  | 민감정보 로그/에러 노출 금지 | 로그 마스킹, 클라이언트에 내부 정보 노출 X | ⭐️⭐️⭐ |
  |  | CORS 제한 | Origin whitelist 등록 | ⭐️⭐ |
  |  | Open Redirect 방지 | redirect 파라미터 검증 | ⭐️ |
  | 🌐 네트워크 보안 | HTTPS 설정 (ALB/TLS) | 암호화된 전송 | ⭐️⭐️⭐ |
  |  | 보안 헤더 설정 (CSP, XFO 등) | 클릭재킹, MIME 공격 방지 | ⭐️⭐ |
  |  | ALB/보안 그룹 최소화 | IP, 포트 최소화 | ⭐️⭐ |
  |  | API Rate Limiting | 무차별 요청 방지 | ⭐️ |
  | 🧱 인프라/운영 보안 | CI/CD Secrets 관리 (GitHub Actions) | 민감정보 보호 | ⭐️⭐️⭐ |
  |  | 환경변수 관리 (.env, AWS Secrets Manager) | 노출 방지 | ⭐️⭐️⭐ |
  |  | 이미지 취약점 검사 (Dependabot, Snyk) | 의존성 보안 강화 | ⭐️⭐ |
  |  | SSH 접근 제어 (My IP만 허용) | 서버 침입 방지 | ⭐️⭐ |
  | 📊 로깅/모니터링 | 기본 로깅 (SLF4J 등) | 요청/응답 기록 | ⭐️⭐️⭐ |
  |  | 에러/예외 로깅 | 문제 추적 | ⭐️⭐️⭐ |
  |  | 실시간 알림 (OneSignal 등) | 이상 행위 탐지 시 유저/관리자에게 알림 | ⭐️ |
  |  | 활동 감사 로그 | 로그인, 결제 등 저장 | ⭐️ |
  | 📦 파일/미디어 보안 | S3/Supabase presigned URL 적용 | 임시 접근 제한 | ⭐️⭐️⭐ |
  |  | URL 만료시간 설정 | 장기 노출 방지 | ⭐️⭐ |
  | 🎛️ 프론트엔드 보안 | dangerouslySetInnerHTML 금지 | DOM XSS 방지 | ⭐️⭐ |
  |  | 소스 난독화 | 코드 분석 방지 | ⭐️ |
  |  | CSP 헤더 설정 | JS/iframe 제한 | ⭐️ |
    
  ---

  ## ✅ 포트폴리오 프로젝트 보안 항목 설명

  ### 🔐 인증/인가

  | 항목 | 설명 |
      | --- | --- |
  | **JWT 인증 및 인가** | 로그인 후 토큰을 발급하고, 이 토큰을 통해 인증된 요청만 처리함. Spring Security에서 `UsernamePasswordAuthenticationFilter` 커스터마이징 필요. |
  | **JWT 무효화 (블랙리스트)** | JWT는 stateless라서 강제 로그아웃이 어려움. Redis 등을 활용해 로그아웃 시 토큰을 블랙리스트에 등록해 검증단계에서 거름. |
  | **비밀번호 암호화 (BCrypt)** | 평문 저장은 위험하므로 `BCryptPasswordEncoder`로 해시화 후 저장. |
  | **Brute Force 방지** | 같은 아이디로 반복 로그인 시도 제한. Redis로 시도 횟수를 카운팅하여 일정 횟수 초과 시 차단. |
  | **역할 기반 권한 제어 (RBAC)** | 관리자는 관리자 페이지, 유저는 본인 정보만 접근 가능하도록 Role 기반 URL 제어. |
  | **WebSocket 인증** | HTTP와 달리 JWT나 세션을 수동으로 검증해야 함. 연결 시 인증 정보 포함 처리 필요. |
    
  ---

  ### 🛡️ 입력/출력 보안

  | 항목 | 설명 |
      | --- | --- |
  | **SQL Injection 방지** | JPA 사용 시 쿼리 파라미터 바인딩을 통해 자동 방어. NativeQuery 사용 시는 직접 파라미터 escape 필요. |
  | **XSS 방어** | 사용자가 입력한 스크립트 태그를 escape하거나 필터링. React는 기본적으로 escaping 하지만 dangerouslySetInnerHTML 사용 시 주의. |
  | **CSRF 방어** | 서버가 발급한 CSRF 토큰을 form이나 헤더에 포함시켜 요청을 검증. 주로 로그인된 사용자의 상태 변경 요청에 적용. |
  | **Open Redirect 방지** | 로그인 후 리디렉션 URL을 받아서 외부로 나가는 URL인지 검증하지 않으면 악성 링크로 유도될 수 있음. whitelisted URL만 허용. |
  | **Referrer 검증** | API 요청이 내부 페이지에서 왔는지 확인. 스크립트를 통한 외부 접근 차단 용도. |
  | **입력값 검증** | @Valid, @NotNull, @Size 등 사용해서 입력값 유효성 검사. |
  | **CORS 정책 설정** | 허용할 origin(도메인)만 명시해서 외부에서 API 접근을 제한함. Spring에서는 `@CrossOrigin` 또는 `WebMvcConfigurer` 사용. |
    
  ---

  ### 🌐 네트워크/웹 보안

  | 항목 | 설명 |
      | --- | --- |
  | **HTTPS 적용 (ALB/SSL)** | AWS ALB에서 HTTPS 인증서 설정 후 포트 443으로 리다이렉트. 민감 정보 암호화 전송. |
  | **보안 헤더 적용 (CSP, HSTS 등)** | 브라우저에 보안 정책을 전달. 예: `Content-Security-Policy`, `X-Content-Type-Options`, `Strict-Transport-Security` 등. |
  | **Clickjacking 방지** | iframe으로 사이트가 삽입되지 않도록 `X-Frame-Options: DENY` 헤더 설정. |
  | **API Rate Limiting** | 사용자당 일정 시간 내 요청 횟수를 제한. Redis를 활용한 IP 또는 사용자 기준 제한 처리. |
  | **ALB/보안 그룹 제한** | 특정 포트만 열고, SSH는 특정 IP만 허용하는 방식으로 방화벽을 설정. |
    
  ---

  ### 🔒 운영/환경 보안

  | 항목 | 설명 |
      | --- | --- |
  | **GitHub Secrets 사용** | `.env` 파일 등을 GitHub에 올리지 않고 `Settings > Secrets`에 등록하여 CI/CD에서 사용. |
  | **환경변수 관리** | Spring Boot에서는 `application.yml`에 민감정보를 직접 쓰지 않고, 외부 환경변수로 주입. |
  | **의존성 보안** | 의존성에 포함된 라이브러리의 취약점을 탐지하고 자동으로 패치하는 도구 사용. 예: GitHub Dependabot, Snyk 등. |
  | **Docker 이미지 보안** | Root 사용자 사용 지양, 공식 이미지 기반 사용, 필요 최소한 패키지만 설치. |
  | **SSH 접근 제한** | Bastion Host 또는 고정 IP만 허용하도록 AWS 보안 그룹 설정. |
    
  ---

  ### 📝 로깅/모니터링

  | 항목 | 설명 |
      | --- | --- |
  | **SLF4J 로그 기록** | 요청/응답, 오류 발생 시 로그 남기기. 필요한 경우 Trace ID로 요청 흐름 추적. |
  | **민감정보 로그 마스킹** | 비밀번호, 카드번호, 주민번호 등은 로그에 남기지 않거나 마스킹. |
  | **감사 로그** | 로그인, 결제, 탈퇴 등의 행위는 기록 보관. |
  | **실시간 이상행위 알림** | 예: 특정 사용자 요청 폭주 시 관리자에게 알림 (OneSignal 활용 가능). |
    
  ---

  ### 📦 파일/미디어 보안

  | 항목 | 설명 |
      | --- | --- |
  | **presigned URL (Supabase 등)** | 직접적인 파일 URL 노출 대신 만료시간이 있는 임시 URL로 접근 제어. |
  | **파일 MIME, 확장자 검증** | .exe, .js 같은 악성 코드 업로드 방지. 클라이언트/서버 양쪽에서 체크. |
    
  ---

  ### 🎛️ 프론트엔드 보안

  | 항목 | 설명 |
      | --- | --- |
  | **React dangerouslySetInnerHTML 금지** | 외부 입력값을 HTML로 렌더링할 경우 XSS에 매우 취약. 가능하면 사용 금지. |
  | **Content Security Policy (CSP)** | 외부 JS/CDN 제한, inline script 방지, 허용 도메인만 명시 가능. |
  | **JS 번들 난독화** | 민감한 클라이언트 코드 노출 방지. 예: 토큰 저장 방식 등. |