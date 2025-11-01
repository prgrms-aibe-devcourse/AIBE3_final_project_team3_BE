plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
}

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
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Lombok dependencies
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Swagger dependencies
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")

    // MongoDB dependencies
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    // 논블로킹 MongoDB 사용 시 아래 의존성 추가 위의 의존성 제거
    // implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    // testImplementation("io.projectreactor:reactor-test")

    // MySQL dependency
    runtimeOnly("com.mysql:mysql-connector-j")

    // Redis dependency
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Testing dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // AWS SDK for S3
    implementation(platform("software.amazon.awssdk:bom:2.24.0"))
    implementation("software.amazon.awssdk:s3")

    // dotenv-java dependency
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
