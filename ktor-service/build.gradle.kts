
plugins {
    id("java")
    kotlin("jvm") version "1.8.22"
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


val ktorVersion = "2.3.5" // Replace with the latest version
val logbackVersion = "1.4.11"

dependencies {
    implementation(Dependencies.MY_BITCOINJ)

    implementation(project(":shared-library"))
    implementation(project(":system-lib"))
    testImplementation(project(":test-utils"))


    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-jackson:1.6.8")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")


    implementation(Dependencies.EXPOSED_CORE)
    implementation(Dependencies.EXPOSED_DAO)
    implementation(Dependencies.EXPOSED_JAVA_TIME)
    implementation(Dependencies.EXPOSED_JDBC)


    implementation("io.ktor:ktor-serialization-gson:$ktorVersion")


    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Use the JUnit 5 integration.
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.3")

//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation("com.google.guava:guava:32.1.1-jre")


}

//// Apply a specific Java toolchain to ease working on different environments.
//java {
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(18))
//    }
//}
//
//application {
//    // Define the main class for the application.
//    mainClass.set("com.undercurrent.AppKt")
//}


tasks.test {
    useJUnitPlatform()
}
