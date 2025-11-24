# Terraform 인프라 가이드

MixChat 서비스의 AWS 인프라를 구성하는 Terraform 코드(`infra/`)를 설명합니다. 운영/개발 담당자가 구조를 빠르게 이해하고 수정할 수 있도록 리소스 목적과 상호 연계를 정리했습니다.

## 1. 개요
- 기본 리전은 `ap-northeast-2`, 모든 리소스는 `var.prefix`(기본값 `team3`) 접두사를 씁니다.
- 하나의 VPC에 3개의 퍼블릭 서브넷(프로덕션/스테이징/일반 EC2), 2개의 프라이빗 서브넷(RDS), 인터넷 게이트웨이, 퍼블릭/프라이빗 라우트 테이블을 둡니다.
- EC2는 Amazon Linux 2023 AMI를 사용하며, `templates/user_data*.tftpl`에서 렌더링한 gzip 압축 user data로 부트스트랩합니다.
- 지속 데이터는 MySQL RDS와 프로덕션/스테이징용 S3 버킷에 저장됩니다.

## 2. 입력 변수
### 2.1 일반 변수 (`infra/variables.tf`)
| 변수 | 기본값 | 설명 |
| --- | --- | --- |
| `region` | `ap-northeast-2` | 리전 및 가용 영역 접미어 기준. |
| `prefix` | `team3` | 리소스/태그 이름 공간. |
| `app_1_domain` | `mixchat.yhcho.com` | 프로덕션 도메인, user data에서 Nginx 설정 등에 사용. |
| `app_2_domain` | `staging.yhcho.com` | 스테이징 도메인. |
| `nickname` | `yhcho14` | S3 버킷 이름 충돌 방지용 후행 문자열. |
| `rds_instance_class` | `db.t3.micro` | RDS 인스턴스 사양. |
| `rds_allocated_storage` | `20` | 초기 스토리지(GB), 최대 100GB까지 자동 확장. |
| `rds_backup_retention_period` | `7` | 자동 백업 유지 일수. |
| `rds_engine_version` | `8.0.43` | MySQL 버전. |
| `rds_multi_az` | `false` | Multi-AZ 활성화 여부. |

### 2.2 민감 변수 (`infra/secrets.tf`)
- DB 접속 정보: `app_1_db_username`, `app_1_db_password`, `app_1_db_name`, `app_2_db_name`.
- 시스템 비밀번호: `password_1`, `password_2` (SSH/Redis/MySQL 초기 설정).
- GitHub 패키지 토큰: `github_access_token_1_owner`, `github_access_token_1`.
> **주의:** 실 운영 시에는 Terraform Cloud 변수, SSM Parameter Store 등 안전한 경로로 주입하고, `secrets.tf` 기본값은 제거하는 것이 좋습니다.

## 3. 네트워킹 (`infra/main.tf`)
### 3.1 VPC
- `aws_vpc.vpc_1`: `10.0.0.0/16` CIDR, DNS hostname/support 활성화로 내부 이름 해석 지원.

### 3.2 서브넷 구성
| 리소스 | CIDR | AZ | 퍼블릭 IP | 용도 |
| --- | --- | --- | --- | --- |
| `subnet_1` | `10.0.0.0/24` | `${region}a` | 예 | 퍼블릭 워크로드 일반용. |
| `subnet_2` | `10.0.1.0/24` | `${region}b` | 예 | 프로덕션 EC2. |
| `subnet_3` | `10.0.2.0/24` | `${region}c` | 예 | 스테이징 EC2. |
| `subnet_5` | `10.0.4.0/24` | `${region}a` | 아니오 | RDS 프라이빗 서브넷. |
| `subnet_6` | `10.0.5.0/24` | `${region}c` | 아니오 | RDS 프라이빗 서브넷. |

### 3.3 인터넷 및 라우팅
- `aws_internet_gateway.igw_1`: VPC 외부 연결 제공.
- `aws_route_table.rt_1`: `0.0.0.0/0` → IGW 경로. `association_1`~`association_3`로 퍼블릭 서브넷에 연결.
- `aws_route_table.rt_private`: 인터넷 경로 없음. `association_5`, `association_6`를 통해 DB 서브넷을 완전 프라이빗으로 유지.

## 4. 보안
### 4.1 시큐리티 그룹
- `aws_security_group.sg_1`: 22/80/443 포트를 전세계에서 허용, 전체 egress 허용. 모든 EC2에 적용.
- `aws_security_group.rds_1`: 3306 포트를 `sg_1`로만 제한하여 애플리케이션 계층만 DB에 접근하도록 제어.

### 4.2 IAM
- `aws_iam_role.ec2_role_1`: EC2 서비스가 AssumeRole 할 수 있는 신뢰 정책.
- 부착 정책:
  - `AmazonS3FullAccess`: 버킷 접근용(필요 시 버킷 리소스로 범위 축소 추천).
  - `service-role/AmazonEC2RoleforSSM`: SSM 세션/패치 관리 허용.
- `aws_iam_instance_profile.instance_profile_1`: 위 역할을 EC2에 주입.

## 5. 컴퓨팅 계층
### 5.1 AMI 조회
- `data.aws_ami.latest_amazon_linux`: Amazon Linux 2023 HVM x86_64, EBS 기반 최신 이미지 필터링.

### 5.2 EC2 인스턴스
| 리소스 | 서브넷 | 목적 | 비고 |
| --- | --- | --- | --- |
| `aws_instance.ec2_1` | `subnet_2` | 프로덕션 노드 | `t3.micro`, 30GB gp3, 공인 IP. |
| `aws_instance.ec2_2` | `subnet_3` | 스테이징 노드 | 동일 IAM/보안그룹/부트스트랩. |

공통 사항:
- `sg_1`과 `instance_profile_1` 적용.
- 루트 볼륨은 gp3 30GB.
- gzip 압축된 user data(`user_data_base64`) 주입.

### 5.3 User Data & 로컬 파일
- `local.compose_support_files`: `docker/config`의 Redis/Mongo 스크립트를 base64로 인코딩하여 EC2에 재생성.
- `local.docker_compose_prod_b64`, `local.app_env_*`: `docker-compose-prod.yml`, `.env`, `.env.prod.properties`, `.env.staging.properties` 내용을 base64로 포함.
- `templatefile()`로 user data 템플릿 렌더링:
  - `user_data.sh.tftpl`: 단일 프로덕션 인스턴스(`ec2_1`).
  - `user_data_2.sh.tftpl`: 스테이징 인스턴스.
- 렌더링 결과는 `base64gzip`으로 압축/인코딩해 EC2 제한을 우회하고 전송 안정성 확보.

## 6. 데이터베이스 계층
### 6.1 서브넷 그룹
- `aws_db_subnet_group.app_1`: `subnet_5`, `subnet_6`를 묶어 RDS가 2개 AZ의 프라이빗 서브넷을 사용하도록 지정.

### 6.2 RDS 인스턴스
- `aws_db_instance.app_1_db` 주요 설정:
  - 엔진/버전: `var.rds_engine_version` (기본 8.0.43).
  - 사양: `var.rds_instance_class` (`db.t3.micro`).
  - 스토리지: 20GB gp3, 최대 100GB 자동 확장.
  - 백업: `var.rds_backup_retention_period`일 유지, 태그 스냅샷 복사.
  - 가용성: `var.rds_multi_az`로 HA 전환 가능.
  - 보안: 비공개 엔드포인트, 스토리지 암호화, 자동 마이너 업데이트, `skip_final_snapshot = true`(운영 시 false 권장).

## 7. 오브젝트 스토리지
- `aws_s3_bucket.prod_bucket`: `${prefix}-prod-${nickname}`, 프로덕션 파일 스토리지. user data에서 버킷 이름을 주입해 애플리케이션 설정.
- `aws_s3_bucket.staging_bucket`: `${prefix}-staging-${nickname}`, 스테이징 전용.
> 향후 버전 관리, 수명 주기 정책, 버킷 정책 강화 및 접근 로깅을 고려하세요.

## 8. 출력값 (`infra/outputs.tf`)
- `prod_db_host`: 프로덕션 MySQL 엔드포인트.
- `prod_db_name`: 스프링 프로필에서 사용하는 DB 이름.
- `prod_s3_bucket`, `staging_s3_bucket`: CI/CD 또는 애플리케이션 설정에서 참조할 버킷 이름.

## 9. 배포 흐름
```bash
cd infra
terraform init
terraform plan -out tfplan
terraform apply tfplan
```
- 변수 값은 `terraform.tfvars`, `-var` 옵션, 환경 변수 등으로 주입하세요.
- 실 서비스에서는 원격 상태 백엔드(Terraform Cloud 등)와 비밀 관리 체계를 반드시 구성하십시오.

## 10. 후속 작업 제안
1. VPC/서브넷, 데이터 플로우 다이어그램을 추가해 이해도 향상.
2. S3/IAM 최소 권한 정책, VPC Flow Logs, CloudWatch 경보 등 보안 및 모니터링 강화.
