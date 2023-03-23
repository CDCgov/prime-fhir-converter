group = "gov.cdc.prime"
version = "0.1"

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.10"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
    `java-library`
    `maven-publish`
    jacoco
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    allWarningsAsErrors = true
    useK2 = false
}

compileTestKotlin.kotlinOptions {
    allWarningsAsErrors = true
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
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
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.2.0")
    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.5")
    implementation("ca.uhn.hapi:hapi-structures-v251:2.3")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:6.2.5")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-validation:6.2.5")
    implementation("commons-io:commons-io:2.11.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
}
