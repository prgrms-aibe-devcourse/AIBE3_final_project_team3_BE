output "prod_db_host" {
  description = "Endpoint address for the production MySQL instance"
  value       = aws_db_instance.app_1_db.address
}

output "prod_db_name" {
  description = "Logical database name used by the production Spring profile"
  value       = aws_db_instance.app_1_db.db_name
}

output "prod_s3_bucket" {
  description = "Bucket name backing production file storage"
  value       = aws_s3_bucket.prod_bucket.bucket
}

# output "staging_s3_bucket" {
#   description = "Bucket name backing staging file storage"
#   value       = aws_s3_bucket.staging_bucket.bucket
# }
