import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot")
	id("io.spring.dependency-management")
	kotlin("jvm")
	kotlin("plugin.spring")
	id("com.gorylenko.gradle-git-properties")
	id("com.github.johnrengelman.shadow")
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenLocal()
	mavenCentral()
}

val exposedVersion: String by project
val h2Version: String by project

dependencies {
	implementation(platform("org.jetbrains.exposed:exposed-bom:${exposedVersion}"))
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("com.h2database:h2:${h2Version}")
	implementation("org.jetbrains.exposed:exposed-jdbc")
	implementation("org.jetbrains.exposed:exposed-dao")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

springBoot {
	mainClass.set("com.example.korm.DemoApplicationKt")
	buildInfo {
		properties {
			name.set("Learn Jetbrains Exposed ")
		}
	}
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
	launchScript()
	layered {
		enabled.set(true)
	}
}
