plugins {
    java
    id("org.springframework.boot") version "4.0.10"
}

group = "com.example.mpazpro"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.10"))
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("migrarCatalogo") {
    group = "application"
    description = "Migra una copia H2 de solo lectura a un catálogo MySQL vacío."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.example.mpazpro.backend.MigracionCatalogoCli")
}

tasks.register<JavaExec>("altaEstudianteLocal") {
    group = "application"
    description = "Crea una cuenta de estudiante en el catálogo MySQL local."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.example.mpazpro.backend.AltaEstudianteLocalCli")
}

tasks.register<JavaExec>("asignarCursoLocal") {
    group = "application"
    description = "Asigna explícitamente un curso existente a un docente."
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.example.mpazpro.backend.AsignacionCursoLocalCli")
}

springBoot {
    mainClass.set("com.example.mpazpro.backend.MpazBackendApplication")
}
