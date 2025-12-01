terraform {
  // aws 라이브러리 불러옴
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
  }
}

# AWS 설정 시작
provider "aws" {
  region = var.region
}
# AWS 설정 끝

# VPC 설정 시작
resource "aws_vpc" "vpc_1" {
  cidr_block = "10.0.0.0/16"

  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "${var.prefix}-vpc-1"
  }
}

resource "aws_subnet" "subnet_1" {
  vpc_id                  = aws_vpc.vpc_1.id
  cidr_block              = "10.0.0.0/24"
  availability_zone       = "${var.region}a"
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.prefix}-subnet-1"
  }
}

resource "aws_subnet" "subnet_2" {
  vpc_id                  = aws_vpc.vpc_1.id
  cidr_block              = "10.0.1.0/24"
  availability_zone       = "${var.region}b"
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.prefix}-subnet-2"
  }
}

resource "aws_subnet" "subnet_3" {
  vpc_id                  = aws_vpc.vpc_1.id
  cidr_block              = "10.0.2.0/24"
  availability_zone       = "${var.region}c"
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.prefix}-subnet-3"
  }
}

resource "aws_subnet" "subnet_4" {
  vpc_id                  = aws_vpc.vpc_1.id
  cidr_block              = "10.0.3.0/24"
  availability_zone       = "${var.region}d"
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.prefix}-subnet-4"
  }
}

resource "aws_subnet" "subnet_5" {
  vpc_id                  = aws_vpc.vpc_1.id
  cidr_block              = "10.0.4.0/24"
  availability_zone       = "${var.region}a"
  map_public_ip_on_launch = false

  tags = {
    Name = "${var.prefix}-subnet-5"
  }
}

resource "aws_subnet" "subnet_6" {
  vpc_id                  = aws_vpc.vpc_1.id
  cidr_block              = "10.0.5.0/24"
  availability_zone       = "${var.region}c"
  map_public_ip_on_launch = false

  tags = {
    Name = "${var.prefix}-subnet-6"
  }
}

resource "aws_internet_gateway" "igw_1" {
  vpc_id = aws_vpc.vpc_1.id

  tags = {
    Name = "${var.prefix}-igw-1"
  }
}

resource "aws_route_table" "rt_1" {
  vpc_id = aws_vpc.vpc_1.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw_1.id
  }

  tags = {
    Name = "${var.prefix}-rt-1"
  }
}

resource "aws_route_table" "rt_private" {
  vpc_id = aws_vpc.vpc_1.id
  tags = {
    Name = "${var.prefix}-rt-private"
  }
}

resource "aws_route_table_association" "association_1" {
  subnet_id      = aws_subnet.subnet_1.id
  route_table_id = aws_route_table.rt_1.id
}

resource "aws_route_table_association" "association_2" {
  subnet_id      = aws_subnet.subnet_2.id
  route_table_id = aws_route_table.rt_1.id
}

resource "aws_route_table_association" "association_3" {
  subnet_id      = aws_subnet.subnet_3.id
  route_table_id = aws_route_table.rt_1.id
}

resource "aws_route_table_association" "association_4" {
  subnet_id      = aws_subnet.subnet_4.id
  route_table_id = aws_route_table.rt_1.id
}

resource "aws_route_table_association" "association_5" {
  subnet_id      = aws_subnet.subnet_5.id
  route_table_id = aws_route_table.rt_private.id
}

resource "aws_route_table_association" "association_6" {
  subnet_id      = aws_subnet.subnet_6.id
  route_table_id = aws_route_table.rt_private.id
}

resource "aws_security_group" "sg_1" {
  name = "${var.prefix}-sg-1"

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "all"
    cidr_blocks = ["0.0.0.0/0"]
  }

  vpc_id = aws_vpc.vpc_1.id

  tags = {
    Name = "${var.prefix}-sg-1"
  }
}

resource "aws_security_group" "rds_1" {
  name        = "${var.prefix}-sg-rds"
  description = "Allow MySQL access from application EC2 instances"
  vpc_id      = aws_vpc.vpc_1.id

  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.sg_1.id]
  }

  ingress {
    description = "Temporary public MySQL access"
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "all"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.prefix}-sg-rds"
  }
}

# EC2 설정 시작

# EC2 역할 생성
resource "aws_iam_role" "ec2_role_1" {
  name = "${var.prefix}-ec2-role-1"

  # 이 역할에 대한 신뢰 정책 설정. EC2 서비스가 이 역할을 가정할 수 있도록 설정
  assume_role_policy = <<EOF
  {
    "Version": "2012-10-17",
    "Statement": [
      {
        "Sid": "",
        "Action": "sts:AssumeRole",
        "Principal": {
            "Service": "ec2.amazonaws.com"
        },
        "Effect": "Allow"
      }
    ]
  }
  EOF

  tags = {
    Name = "${var.prefix}-ec2-role-1"
  }
}

# EC2 역할에 AmazonS3FullAccess 정책을 부착
resource "aws_iam_role_policy_attachment" "s3_full_access" {
  role       = aws_iam_role.ec2_role_1.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
}

# EC2 역할에 AmazonEC2RoleforSSM 정책을 부착
resource "aws_iam_role_policy_attachment" "ec2_ssm" {
  role       = aws_iam_role.ec2_role_1.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEC2RoleforSSM"
}

# IAM 인스턴스 프로파일 생성
resource "aws_iam_instance_profile" "instance_profile_1" {
  name = "${var.prefix}-instance-profile-1"
  role = aws_iam_role.ec2_role_1.name

  tags = {
    Name = "${var.prefix}-instance-profile-1"
  }
}

locals {
  compose_support_files = [
    {
      path        = "/opt/mixchat/docker/config/redis/init.redis"
      content_b64 = base64encode(file("${path.module}/../docker/config/redis/init.redis"))
    },
    {
      path        = "/opt/mixchat/docker/config/mongo/schema.js"
      content_b64 = base64encode(file("${path.module}/../docker/config/mongo/schema.js"))
    },
    {
      path        = "/opt/mixchat/docker/config/mongo/init.js"
      content_b64 = base64encode(file("${path.module}/../docker/config/mongo/init.js"))
    }
  ]

  docker_compose_prod_b64        = base64encode(file("${path.module}/../docker-compose-prod.yml"))
#  docker_compose_staging_b64     = base64encode(file("${path.module}/../docker-compose-staging.yml"))
  app_env_prod_b64               = base64encode(file("${path.module}/../.env"))
  app_env_prod_properties_b64    = base64encode(file("${path.module}/../.env.prod.properties"))
#  app_env_staging_properties_b64 = base64encode(file("${path.module}/../.env.staging.properties"))
  application_secret_yaml_b64    = base64encode(file("${path.module}/../src/main/resources/application-secret.yml"))

  ec2_1_user_data_base = templatefile("${path.module}/templates/user_data.sh.tftpl", {
    password_1                  = var.password_1
    app_1_domain                = var.app_1_domain
    app_1_db_name               = var.app_1_db_name
    app_1_db_username           = var.app_1_db_username
    app_1_db_password           = var.app_1_db_password
    app_1_db_host               = aws_db_instance.app_1_db.address
    github_access_token_1_owner = var.github_access_token_1_owner
    github_access_token_1       = var.github_access_token_1
    docker_compose_prod_b64     = local.docker_compose_prod_b64
    app_env_prod_b64            = local.app_env_prod_b64
    app_env_prod_properties_b64 = local.app_env_prod_properties_b64
    compose_support_files       = [for file in local.compose_support_files : {
      path        = file.path
      content_b64 = file.content_b64
    }]
    app_1_s3_bucket              = aws_s3_bucket.prod_bucket.bucket
    application_secret_yaml_b64  = local.application_secret_yaml_b64
  })

#  ec2_2_user_data_base = templatefile("${path.module}/templates/user_data_2.sh.tftpl", {
#    password_2                     = var.password_2
#    app_2_domain                   = var.app_2_domain
#    app_2_db_name                  = var.app_2_db_name
#    github_access_token_1_owner    = var.github_access_token_1_owner
#    github_access_token_1          = var.github_access_token_1
#    docker_compose_prod_b64        = local.docker_compose_prod_b64
#    docker_compose_staging_b64     = local.docker_compose_staging_b64
#    app_env_prod_b64               = local.app_env_prod_b64
#    app_env_prod_properties_b64    = local.app_env_prod_properties_b64
#    app_env_staging_properties_b64 = local.app_env_staging_properties_b64
#    compose_support_files           = [for file in local.compose_support_files : {
#      path        = file.path
#      content_b64 = file.content_b64
#    }]
#    app_2_s3_bucket             = aws_s3_bucket.staging_bucket.bucket
#    application_secret_yaml_b64 = local.application_secret_yaml_b64
#  })

  ec2_1_user_data_base64 = base64gzip(local.ec2_1_user_data_base)
#  ec2_2_user_data_base64 = base64gzip(local.ec2_2_user_data_base)
}

# 최신 Amazon Linux 2023 AMI 조회 (프리 티어 호환)
data "aws_ami" "latest_amazon_linux" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }

  filter {
    name   = "architecture"
    values = ["x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }

  filter {
    name   = "root-device-type"
    values = ["ebs"]
  }
}

# EC2 인스턴스 생성
resource "aws_instance" "ec2_1" {
  # 사용할 AMI ID
  ami = data.aws_ami.latest_amazon_linux.id
  # EC2 인스턴스 유형
  instance_type = "t3.micro"
  # 사용할 서브넷 ID
  subnet_id = aws_subnet.subnet_2.id
  # 적용할 보안 그룹 ID
  vpc_security_group_ids = [aws_security_group.sg_1.id]
  # 퍼블릭 IP 연결 설정
  associate_public_ip_address = true

  # 인스턴스에 IAM 역할 연결
  iam_instance_profile = aws_iam_instance_profile.instance_profile_1.name

  # 인스턴스에 태그 설정
  tags = {
    Name = "${var.prefix}-ec2-production-1"
  }

  # 루트 볼륨 설정
  root_block_device {
    volume_type = "gp3"
    volume_size = 30 # 볼륨 크기를 12GB로 설정
  }

  user_data_base64 = local.ec2_1_user_data_base64
}

resource "aws_db_subnet_group" "app_1" {
  name       = "${var.prefix}-rds-subnet-group"
  subnet_ids = [aws_subnet.subnet_1.id, aws_subnet.subnet_3.id]

  tags = {
    Name = "${var.prefix}-rds-subnet-group"
  }
}

resource "aws_db_instance" "app_1_db" {
  identifier                 = "${var.prefix}-prod-db"
  engine                     = "mysql"
  engine_version             = var.rds_engine_version
  instance_class             = var.rds_instance_class
  allocated_storage          = var.rds_allocated_storage
  max_allocated_storage      = 100
  db_name                    = var.app_1_db_name
  username                   = var.app_1_db_username
  password                   = var.app_1_db_password
  port                       = 3306
  db_subnet_group_name       = aws_db_subnet_group.app_1.name
  vpc_security_group_ids     = [aws_security_group.rds_1.id]
  storage_type               = "gp3"
  backup_retention_period    = var.rds_backup_retention_period
  copy_tags_to_snapshot      = true
  multi_az                   = var.rds_multi_az
  publicly_accessible        = true
  storage_encrypted          = true
  deletion_protection        = false
  auto_minor_version_upgrade = true
  apply_immediately          = true
  skip_final_snapshot        = true

  tags = {
    Name = "${var.prefix}-prod-db"
  }
}

# S3 버킷 설정 시작
resource "aws_s3_bucket" "prod_bucket" {
  bucket = "${var.prefix}-prod-${var.nickname}"

  tags = {
    Name = "${var.prefix}-prod-bucket"
  }
}
# S3 버킷 설정 끝
