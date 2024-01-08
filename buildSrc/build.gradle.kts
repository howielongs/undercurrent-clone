import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    `kotlin-dsl`
//    id("com.github.ben-manes.versions") version "0.39.0"
//    id("ca.cutterslade.analyze") version "1.7.1"

}

tasks.named<KotlinCompilationTask<KotlinJvmCompilerOptions>>("compileKotlin").configure {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

java {
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("check-lib-versions") {
            id = "check-lib-versions"
            implementationClass = "CheckLibVersionsPlugin"
        }
    }
}
