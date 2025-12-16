### 배포 구조
- EC2 인스턴스 개수
    1. 스테이징 서버(release 브랜치)
    2. 배포 서버(main 브랜치)
    3. npm(nginx proxy manager) 서버
- 기타 설정
    - 스테이징 DB는 EC2 컨테이너 안에 쓰기 / 배포 DB는 RDS 사용
    - 스테이징/배포 각각 s3 버킷 하나씩 사용

### 배포 설정
- 배포 서버(main 브랜치)
    - .env.prod.properties
    - application-prod.yml
    - docker-compose-prod.yml
    - 프론트 도메인: mixchat.yhcho.com
    - api 도메인 : api.mixchat.yhcho.com
- 스테이징 서버(release 브랜치)
    - .env.staging.properties
    - application-staging.yml
    - docker-compose-staging.yml
    - 프론트 도메인: staging.mixchat.yhcho.com
    - api 도메인 : api.staging.mixchat.yhcho.com
    - 참고: Terraform user-data 스크립트가 S3 버킷 이름 등 일부 환경변수를 실제 인프라 값으로 덮어쓴다
