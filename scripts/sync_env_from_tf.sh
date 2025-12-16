#!/bin/bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
INFRA_DIR="${REPO_ROOT}/infra"
ENV_FILE="${REPO_ROOT}/.env.prod.properties"

cd "${INFRA_DIR}"
terraform init -backend=false >/dev/null
TF_OUTPUT_JSON=$(terraform output -json)

MYSQL_HOST=$(echo "${TF_OUTPUT_JSON}" | jq -r '.prod_db_host.value')
AWS_S3_BUCKET=$(echo "${TF_OUTPUT_JSON}" | jq -r '.prod_s3_bucket.value')

cd "${REPO_ROOT}"
if grep -q '^MYSQL_HOST=' "${ENV_FILE}"; then
  sed -i "s|^MYSQL_HOST=.*|MYSQL_HOST=${MYSQL_HOST}|" "${ENV_FILE}"
else
  echo "MYSQL_HOST=${MYSQL_HOST}" >> "${ENV_FILE}"
fi

if grep -q '^AWS_S3_BUCKET=' "${ENV_FILE}"; then
  sed -i "s|^AWS_S3_BUCKET=.*|AWS_S3_BUCKET=${AWS_S3_BUCKET}|" "${ENV_FILE}"
else
  echo "AWS_S3_BUCKET=${AWS_S3_BUCKET}" >> "${ENV_FILE}"
fi

echo "Updated ${ENV_FILE} from Terraform outputs."
