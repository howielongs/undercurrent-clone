import java.util.*

plugins {
    id("java")
    kotlin("jvm") version "1.8.22"
//    id("com.github.ben-manes.versions") version "0.39.0"
//    id("ca.cutterslade.analyze") version "1.7.1"

}

repositories {
    mavenLocal()
    mavenCentral()
    maven { setUrl("https://jitpack.io") }

    fun addOrgPackageRepo(repoName: String = "") {
        val baseUrl = "https://maven.pkg.github.com/"
        val orgName = "undercurrent-ai"
        val repoUrl = "$baseUrl$orgName/$repoName"

        maven {
            name = "GitHubPackages"
            url = uri(repoUrl)
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }

    // Add GitHub Packages repositories
    addOrgPackageRepo("bitcoinj")
}
val protobufVersion = Versions.PROTOBUF_JAVALITE
val ktorVersion = Versions.KTOR

dependencies {
    implementation(Dependencies.MY_BITCOINJ)

    implementation(project(":lib"))

    implementation(project(":shared-library"))
    implementation(project(":prompting-library"))
//    implementation(project(":swap-service")) // for passing data up to the swap service
    testImplementation(project(":test-utils"))

//    implementation("com.github.hkirk", "java-html2image", "0.9")

    implementation("software.amazon.awssdk:secretsmanager:2.20.93")
    implementation("software.amazon.awssdk:ssm:2.20.92")
    implementation("org.jasypt:jasypt:1.9.3")


    implementation(Dependencies.FLYWAY_CORE)
    implementation(Dependencies.HIKARI_CP)


    implementation("com.stripe:stripe-java:22.6.0")

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    implementation(Dependencies.KOTLIN_RESULT)


    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-serialization-gson:$ktorVersion")

    implementation("com.google.protobuf:protobuf-kotlin:3.21.7")

    implementation("com.google.protobuf:protobuf-javalite:$protobufVersion")

    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.21")

    // This dependency is used by the application.
    implementation(Dependencies.GUAVA)

    // Use the Kotlin test library.
//    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
//    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    implementation(Dependencies.EXPOSED_CORE)
    implementation(Dependencies.EXPOSED_DAO)
    implementation(Dependencies.EXPOSED_JAVA_TIME)
    implementation(Dependencies.EXPOSED_JDBC)

// https://mavenlibs.com/maven/dependency/com.googlecode.libphonenumber/libphonenumber
    implementation(Dependencies.LIB_PHONE_NUMBER)

    implementation("org.json", "json", "20220320")
    implementation(Dependencies.EMOJI_JAVA)
    implementation("io.github.shashankn", "qr-terminal", "1.0.0")


    testImplementation(Dependencies.JUNIT_JUPITER_API)

        testImplementation(Dependencies.JUNIT_JUPITER_ENGINE)

    implementation("net.sourceforge.argparse4j", "argparse4j", "0.8.1")

    implementation(Dependencies.SQLITE_JDBC)

    implementation(libs.bouncycastle)
    implementation(libs.jackson.databind)
    implementation(libs.argparse4j)
    implementation(libs.dbusjava)
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.jul)
    implementation(libs.logback)
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")

}

tasks.test {
    useJUnitPlatform()
}

val properties = Properties().apply {
    load(File("gradle.properties").inputStream())
}

fun getPropertySafe(propertyName: String): String {
    return try {
        property(propertyName).toString()
    } catch (e: Exception) {
        println("Warning: Property $propertyName not found.")
        ""
    }
}

tasks.withType<JavaExec> {
    jvmArgs(
        "-Dmob_live_mnemonic=${getPropertySafe("mob_live_mnemonic")}",
        "-Dmob_qa_mnemonic=${getPropertySafe("mob_qa_mnemonic")}",
        "-Dmob_dev_mnemonic=${getPropertySafe("mob_dev_mnemonic")}",
        "-Dadmin_sms_1=${getPropertySafe("admin_sms_1")}",
        "-Dadmin_sms_2=${getPropertySafe("admin_sms_2")}",
    )
}

