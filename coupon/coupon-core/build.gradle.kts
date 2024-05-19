val bootJar: org.springframework.boot.gradle.tasks.bundling.BootJar by tasks

bootJar.enabled = false

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    // redis의 분산락을 구현하기 위해 redisson을 사용
    implementation("org.redisson:redisson-spring-boot-starter:3.30.0")

    // 레디스 캐싱을 위한 jackson 라이브러리 추가
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // 로컬 캐시 구현을 위한 caffeine 라이브러리 추가
    implementation("com.github.ben-manes.caffeine:caffeine")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
