plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
}
val springCloudVersion by extra("2025.0.0")
val springAiVersion by extra("1.1.0")

group = "triplestar"
version = "0.0.1-SNAPSHOT"
description = "mixchat"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.cloud:spring-cloud-function-context")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.ai:spring-ai-starter-model-ollama")
    //implementation("org.springframework.ai:spring-ai-starter-model-vertex-ai-gemini")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

    // MongoDB
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // MySQL
    runtimeOnly("com.mysql:mysql-connector-j")

    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // flyway
    implementation("org.flywaydb:flyway-core")

    // Minio
    implementation("io.minio:minio:8.5.3")

    // Testcontainers BOM
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.8"))

    // Testcontainers
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:mongodb")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // AWS SDK for S3
    implementation(platform("software.amazon.awssdk:bom:2.24.0"))
    implementation("software.amazon.awssdk:s3")

    // Spring security
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.security:spring-security-test")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // dotenv-java dependency
    implementation("io.github.cdimascio:dotenv-java:3.0.0")

    // websocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}