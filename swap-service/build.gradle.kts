plugins {
    id("java")
    kotlin("jvm") version "1.8.22"
    application
}


//  gradle :swap-service:installDist
//  ./build/install/swap-service/bin/swap-service

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


    implementation(project(":shared-library"))
    implementation(project(":prompting-library"))
    implementation(project(":system-lib"))
    testImplementation(project(":test-utils"))

    implementation(Dependencies.LOG4J_API)
    implementation(Dependencies.LOG4J_CORE)
       implementation(Dependencies.LOG4J_SLF4J_IMPL)
 //runtimeOnly(Dependencies.LOG4J_ASYNC)

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation(Dependencies.JUNIT_JUPITER)

    implementation(Dependencies.KOTLIN_RESULT)


    implementation(Dependencies.FLYWAY_CORE)
    implementation(Dependencies.HIKARI_CP)

    implementation(Dependencies.SQLITE_JDBC)

    implementation(Dependencies.EXPOSED_CORE)
    implementation(Dependencies.EXPOSED_DAO)
    implementation(Dependencies.EXPOSED_JAVA_TIME)
    implementation(Dependencies.EXPOSED_JDBC)

    implementation(Dependencies.LIB_PHONE_NUMBER)

    // Use the Kotlin JUnit 5 integration.
    testImplementation(Dependencies.KOTLIN_TEST_JUNIT_5)

    // Use the JUnit 5 integration.
    testImplementation(Dependencies.JUNIT_JUPITER_ENGINE)

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api(Dependencies.COMMONS_MATH_3)

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation(Dependencies.GUAVA)
    testImplementation(project(mapOf("path" to ":shared-library")))
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.undercurrent.swaps.start.Main") // Replace with your actual main class
}