group = "gov.cdc.prime"
version = "0.1"

// gradlew wasn't working https://stackoverflow.com/questions/17668265/gradlew-permission-denied, might need to push to codebase?
plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    `java-library`
    `maven-publish`
    jacoco
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}
val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "17"
    allWarningsAsErrors = true
    useK2 = false
}

compileTestKotlin.kotlinOptions {
    jvmTarget = "17"
    allWarningsAsErrors = true
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jar {
    manifest {
        attributes(
            mapOf("Implementation-Title" to project.name, "Implementation-Version" to project.version)
        )
    }
    finalizedBy("kotlinSourcesJar")
    finalizedBy("shadowJar")
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    // Match the ktlint version used by ReportStream while the library migration is taking place
    // See ktlint versions at https://github.com/pinterest/ktlint/releases
    version.set("0.44.0")
    disabledRules.set(setOf("final-newline"))
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("prime-fhirconverter") {
            groupId = "gov.cdc.prime"
            artifactId = "fhirconverter"
            version = "0.1-SNAPSHOT"

            from(components["java"])
        }
    }
}

val jacksonVersion = "2.14.1"
dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    implementation("org.apache.logging.log4j:log4j-api:[2.17.1,)")
    implementation("org.apache.logging.log4j:log4j-core:[2.17.1,)")
    api("org.apache.logging.log4j:log4j-api-kotlin:1.2.0")
    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.5")
    api("ca.uhn.hapi:hapi-structures-v251:2.3")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:8.0.0")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-caching-caffeine:8.0.0") // added to avoid "HAPI-2200: No Cache Service Providers found. Choose between hapi-fhir-caching-caffeine (Default) and hapi-fhir-caching-guava (Android)" caused in fhirpathutils
    implementation("ca.uhn.hapi.fhir:hapi-fhir-validation:8.0.0") // bump not needed (currently debugging tests)
    implementation("commons-io:commons-io:2.11.0")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    api("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.28.1") // bumping for isNotSameInstanceAs/isSameInstanceAs
}
