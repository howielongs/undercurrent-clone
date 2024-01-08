plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
}


dependencyResolutionManagement {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    versionCatalogs {
        create("libs") {
            library("bouncycastle", "org.bouncycastle", "bcprov-jdk18on").version("1.76")
            library("jackson.databind", "com.fasterxml.jackson.core", "jackson-databind").version("2.15.3")
            library("argparse4j", "net.sourceforge.argparse4j", "argparse4j").version("0.9.0")
            library("dbusjava", "com.github.hypfvieh", "dbus-java-transport-native-unixsocket").version("4.3.1")
            version("slf4j", "2.0.9")
            library("slf4j.api", "org.slf4j", "slf4j-api").versionRef("slf4j")
            library("slf4j.jul", "org.slf4j", "jul-to-slf4j").versionRef("slf4j")
            library("logback", "ch.qos.logback", "logback-classic").version("1.4.11")


            library("signalservice", "com.github.turasa", "signal-service-java").version("2.15.3_unofficial_86")
            library("sqlite", "org.xerial", "sqlite-jdbc").version("3.43.0.0")
            library("hikari", "com.zaxxer", "HikariCP").version("5.0.1")
            library("junit", "org.junit.jupiter", "junit-jupiter").version("5.10.0")
        }
    }
}

rootProject.name = "signal-cli"
include(
    "lib",
    "shared-library",
    "shop-service",
    "prompting-library",
    "ktor-service",
    "swap-service",
    "system-lib",
    "test-utils",
    "test-service",
    )
