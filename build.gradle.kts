import versioning.BuildConfig
import versioning.VersionUtil

plugins {
    id("java-library")
    kotlin("jvm") version "2.1.0" apply false
}

BuildConfig.init(project)

val baseVersion = "2.3.74"
group = "ac.grim.grimac"
version = VersionUtil.computeVersion(project, baseVersion)
description = "Libre simulation anticheat designed for 26.2 with 1.8–26.2 support, powered by PacketEvents 2.0."

// Typsichere Datenstruktur statt veraltetem 'ext'
class VersionData {
    val timestamp = System.currentTimeMillis().toString()
    val gitBranch = VersionUtil.getGitBranch(project, true)
    val gitCommit = VersionUtil.getGitCommitHash(project, true)
    val gitOrg = System.getenv("GRIM_GIT_ORG") ?: VersionUtil.getGitUser(project)
    val gitRepo = System.getenv("GRIM_GIT_REPO") ?: "Grim"
}
val buildInfo = VersionData()

logger.lifecycle("""
    Build configuration:
        shadePE            = ${BuildConfig.shadePE}
        relocate           = ${BuildConfig.relocate}
        mavenLocalOverride = ${BuildConfig.mavenLocalOverride}
        release            = ${BuildConfig.release}
        version            = $version
""".trimIndent())

tasks.register("printVersion") {
    group = "versioning"
    description = "Prints the computed project version"
    doLast {
        println("VERSION=$version")
    }
}

subprojects {
    // Optimiert die Kompilierung für alle Sprachen (Java & Kotlin)
    tasks.withType<JavaCompile>().configureEach {
        options.isFork = true
        options.isIncremental = true
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        java {
            toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        }
        
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
                freeCompilerArgs.add("-Xjsr305=strict")
            }
        }
    }
}
