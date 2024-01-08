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

dependencies {
    implementation(project(":shared-library"))
    implementation(project(":prompting-library"))
    implementation(project(":system-lib"))
    testImplementation(project(":test-utils"))

    implementation(Dependencies.KOTLIN_RESULT)
    implementation(Dependencies.SQLITE_JDBC)

    implementation(Dependencies.EXPOSED_CORE)
    implementation(Dependencies.EXPOSED_DAO)
    implementation(Dependencies.EXPOSED_JAVA_TIME)
    implementation(Dependencies.EXPOSED_JDBC)

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation(Dependencies.JUNIT_JUPITER)
    testImplementation(Dependencies.KOTLIN_TEST_JUNIT_5)
    testImplementation(Dependencies.JUNIT_JUPITER_ENGINE)
}

tasks.test {
    useJUnitPlatform()
}
