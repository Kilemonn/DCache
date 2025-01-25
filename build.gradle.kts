var springVersion = "3.4.1"
val testContainersVersion = "1.20.4"

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.0"
    id("org.springframework.boot") version "3.3.0"
    jacoco
}

group = "au.kilemonn"
version = "0.1.0"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web:${springVersion}")
    implementation("org.springframework.boot:spring-boot-starter-data-redis:${springVersion}")

    // https://mvnrepository.com/artifact/com.github.ben-manes.caffeine/caffeine
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    // https://mvnrepository.com/artifact/com.googlecode.xmemcached/xmemcached
    implementation("com.googlecode.xmemcached:xmemcached:2.4.8")

    // Runtime
    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-reflect
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:2.1.0")

    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
    testImplementation("org.springframework.boot:spring-boot-starter-test:${springVersion}")

    // https://mvnrepository.com/artifact/org.testcontainers/testcontainers
    testImplementation("org.testcontainers:testcontainers:${testContainersVersion}")
    testImplementation("org.testcontainers:junit-jupiter:${testContainersVersion}")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

kotlin {
    jvmToolchain(17)
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // Generate the report after the tests
    reports {
        xml.required.set(false)
        csv.required.set(true)
    }
}

// Disable bootjar task since this is a library and has no main class
tasks.bootJar {
    enabled = false
}
