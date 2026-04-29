plugins {
    java
    alias(libs.plugins.springboot)
    alias(libs.plugins.spotless)
}

group = "com.app"
version = "0.0.1-SNAPSHOT"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(25)) }
}

dependencies {
    implementation(platform(libs.sb.bom))

    implementation(libs.bundles.springweb)
    implementation(libs.bundles.persistence.pg)
    implementation(libs.sb.starter.data.redis)

    implementation("com.app:shared-common:1.0.0-SNAPSHOT")
    implementation("com.app:shared-security:1.0.0-SNAPSHOT")
    
    implementation(libs.springdoc.openapi.webmvc)
    
    developmentOnly(platform(libs.sb.bom))
    developmentOnly(libs.sb.docker.compose)
    developmentOnly(libs.sb.devtools)

    compileOnly(libs.lombok)
    annotationProcessor(platform(libs.sb.bom))
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.sb.configuration.processor)

    testImplementation(libs.bundles.testbundle)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.removeAll { it == "--enable-preview" }
}

spotless {
    java {
        googleJavaFormat("1.27.0")
        removeUnusedImports()
    }
}

tasks.bootBuildImage { environment.put("BP_JVM_CDS_ENABLED", "true") }


tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> { systemProperties(System.getProperties().map { it.key.toString() to it.value }.toMap()) }

tasks.register<Exec>("stopApp") {
    group = "application"
    description = "Stops the running auth-service application."
    commandLine("sh", "-c", "lsof -t -i:8081 | xargs kill -9 || true")
}

tasks.clean {
    mustRunAfter("spotlessApply")
}
