version = "1.0.0"
group = "com.undercurrent"

val repoName = "shared-library"

plugins {
    id("java")
    kotlin("jvm") version "1.8.22"
//    id("com.github.ben-manes.versions") version "0.39.0"
//    id("ca.cutterslade.analyze") version "1.7.1"

    `maven-publish`
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

//publishing {
//    // publish or publishToMavenLocal
//    repositories {
//        maven {
//            name = "GitHubPackages"
//            url = uri("https://maven.pkg.github.com/undercurrent-ai/$repoName")
//            credentials {
//                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
//                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
//            }
//        }
//    }
//    publications {
//        register<MavenPublication>("gpr") {
//            groupId = "$group"       // Set the groupId
//            artifactId = "$repoName"     // Set the artifactId
//            version = "$version"             // Set the version
//
//            from(components["java"])
//        }
//    }
//}

repositories {
    // ./gradlew publishToMavenLocal
    // ./gradlew publish

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

    // Add GitHub Packages repositories
//    addOrgPackageRepo(signalDaemonSvcName)
//    addOrgPackageRepo(messagingEndpointLibName)
}

dependencies {
    implementation(Dependencies.EMOJI_JAVA)

    implementation(Dependencies.MY_BITCOINJ)
    implementation(Dependencies.KOTLIN_RESULT)

    implementation(Dependencies.SQLITE_JDBC)

    implementation(Dependencies.FLYWAY_CORE)
    implementation(Dependencies.HIKARI_CP)

    implementation(Dependencies.EXPOSED_CORE)
    implementation(Dependencies.EXPOSED_DAO)
    implementation(Dependencies.EXPOSED_JAVA_TIME)
    implementation(Dependencies.EXPOSED_JDBC)

    implementation(Dependencies.LIB_PHONE_NUMBER)

    implementation(Dependencies.LOG4J_API)
    implementation(Dependencies.LOG4J_CORE)
    implementation(Dependencies.LOG4J_SLF4J_IMPL)
    //runtimeOnly(Dependencies.LOG4J_ASYNC)

    testImplementation(Dependencies.JUNIT_JUPITER)
    // Use the Kotlin JUnit 5 integration.
    testImplementation(Dependencies.KOTLIN_TEST_JUNIT_5)

    // Use the JUnit 5 integration.
    testImplementation(Dependencies.JUNIT_JUPITER_ENGINE)
    testImplementation(platform("org.junit:junit-bom:5.9.1"))

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api(Dependencies.COMMONS_MATH_3)

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation(Dependencies.GUAVA)

}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(18))
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}
