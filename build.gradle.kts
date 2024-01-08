import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.protobuf
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.util.*

plugins {
    java
    application
    eclipse
    `check-lib-versions`
    id("org.graalvm.buildtools.native") version "0.9.28"
    kotlin("jvm") version "1.8.22"
    id("com.google.protobuf") version "0.9.1"
    id("com.jetbrains.exposed.gradle.plugin") version "0.2.1"
//    id("com.github.ben-manes.versions") version "0.39.0"
//    id("ca.cutterslade.analyze") version "1.7.1"
}

//subprojects {
//    apply(plugin = "com.github.ben-manes.versions")
//    apply(plugin = "ca.cutterslade.analyze")
//}

val properties = Properties().apply {
    load(File("gradle.properties").inputStream())
}

val protobufVersion = Versions.PROTOBUF_JAVALITE
val ktorVersion = Versions.KTOR

version = properties.getProperty("version") ?: "1.7.0"
group = properties.getProperty("group") ?: "com.undercurrent"

java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18
}

application {
    mainClass.set("org.asamk.signal.Main")
}

graalvmNative {
    binaries {
        this["main"].run {
            resources.autodetect()
            configurationFileDirectories.from(file("graalvm-config-dir"))
            if (System.getenv("GRAALVM_HOME") == null) {
                toolchainDetection.set(true)
                javaLauncher.set(javaToolchains.launcherFor {
                    languageVersion.set(JavaLanguageVersion.of(18))
                })
            } else {
                toolchainDetection.set(false)
            }
        }
    }
}

//val undercurrentBitcoinjName = "bitcoinj"
//val undercurrentBitcoinjVersion = "1.0.0"

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


dependencies {
    implementation(Dependencies.MY_BITCOINJ)

    protobuf(files("build/gen/"))

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

    implementation("net.sourceforge.argparse4j", "argparse4j", "0.8.1")

    implementation(Dependencies.SQLITE_JDBC)

    implementation(libs.bouncycastle)
    implementation(libs.jackson.databind)
    implementation(libs.argparse4j)
    implementation(libs.dbusjava)
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.jul)
    implementation(libs.logback)

    testImplementation("junit:junit:4.13.1")

    implementation(project(":lib"))

    implementation(project(":shared-library"))
    implementation(project(":shop-service"))
    implementation(project(":prompting-library"))

    implementation(project(":ktor-service"))

    implementation(project(":system-lib"))
    implementation(project(":swap-service"))
    implementation(project(":test-service"))
    testImplementation(project(":test-utils"))
    testImplementation(project(":test-service"))


}

configurations {
    implementation {
        resolutionStrategy.failOnVersionConflict()
    }
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    }
}



tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version,
            "Main-Class" to application.mainClass.get()
        )
    }
}

task("fatJar", type = Jar::class) {
    archiveBaseName.set("${project.name}-fat")
    exclude(
        "META-INF/*.SF",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "META-INF/NOTICE*",
        "META-INF/LICENSE*",
        "META-INF/INDEX.LIST",
        "**/module-info.class"
    )
    duplicatesStrategy = DuplicatesStrategy.WARN
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get())
}


protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.0.0"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.0.0-pre2"
        }
    }
    generateProtoTasks {
        ofSourceSet("grpc").forEach { task ->
            task.plugins {
                id("grpc") {
                    outputSubDir = "grpc_output"
                }
            }
            task.generateDescriptorSet = true
        }
    }
}

val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "18"
}
val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "18"
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().resources.srcDirs) {
        include("**")
        expand("version" to version.toString())
    }
}


// Define a custom task to run tests from 'shop-service' and 'shared-lib1' modules.
tasks.register("runSelectedTests") {
    // Depending on the 'test' task of the current ('app') module.
    dependsOn("test")

    // Depending on the 'test' task of the 'shop-service' and 'shared-library' modules.
    dependsOn(":prompting-library:test")
    dependsOn(":shop-service:test")
    dependsOn(":shared-library:test")
    dependsOn(":swap-service:test")
    dependsOn(":system-lib:test")
    dependsOn(":test-service:test")
    dependsOn(":test-utils:test")

}

// Ensure that the 'build' task of the 'app' module depends on the custom 'runSelectedTests' task.
tasks.named("build") {
    dependsOn("runSelectedTests")
}

//  ./gradlew :app:test
tasks.test {
    // Display each individual test result in the console.
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

tasks.register("allDependencies") {
    subprojects.forEach { subproject ->
        dependsOn("${subproject.path}:dependencies")
    }
}
