# GitHub Container Registry 설정 가이드

## 문제: "manifest unknown" 오류

CI 워크플로우에서 Docker 이미지를 pull할 수 없는 경우, 패키지가 private으로 설정되어 있을 가능성이 높습니다.

## 해결 방법

### 1. GitHub Container Registry 패키지를 Public으로 설정

1. GitHub 저장소로 이동
2. 우측 사이드바에서 **Packages** 섹션 찾기
3. 또는 직접 URL 접속: `https://github.com/yhcho14?tab=packages`
4. `aibe3_final_project_team3_be` 패키지 클릭
5. 패키지 설정 페이지에서 **Package settings** 클릭
6. **Change visibility** 섹션에서 **Change visibility** 버튼 클릭
7. **Public**을 선택하고 확인

### 2. 패키지가 저장소와 연결되어 있는지 확인

1. 패키지 페이지에서 **Connect repository** 버튼 확인
2. 이 저장소(`AIBE3_final_project_team3_BE`)와 연결

### 3. 워크플로우 권한 확인

1. 저장소 Settings > Actions > General
2. **Workflow permissions** 섹션에서
3. **Read and write permissions** 선택
4. **Allow GitHub Actions to create and approve pull requests** 체크

## 확인 방법

다음 명령어로 이미지가 public으로 설정되었는지 확인:

```bash
docker pull ghcr.io/yhcho14/aibe3_final_project_team3_be:dev-latest
```

로그인 없이 pull이 가능하면 성공!

