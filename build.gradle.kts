plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.plugin.spring)
	alias(libs.plugins.springframework.boot)
	alias(libs.plugins.dependency.management)
}

group = "no.roar.kafka.retry"
version = "0.0.1-SNAPSHOT"

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-kafka")
	runtimeOnly("tools.jackson.module:jackson-module-kotlin:3.0.2") // TODO

	// test
	testImplementation(libs.kotest.runner.junit5)
	testImplementation(libs.kotest.assertions.core)
	testImplementation(libs.kotest.extensions.spring)
	testImplementation(libs.mockk)
	testImplementation(libs.springmockk)

	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(module = "mockito-core")
	}
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.testcontainers:testcontainers-kafka")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	jvmToolchain(21)
	compilerOptions {
		freeCompilerArgs.add("-Xjsr305=strict")
	}
}

tasks.test {
	jvmArgs(
		"-Xshare:off",
		"-XX:+EnableDynamicAgentLoading",
		"-Dkotest.framework.classpath.scanning.autoscan.disable=true",
		"-Dkotest.framework.config.fqn=no.roar.kafka.retry.KotestConfig"
	)
	useJUnitPlatform()
}
