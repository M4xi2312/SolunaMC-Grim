/**
 * GrimAC Build Configuration
 *
 * Build Flags:
 * -PshadePE=true   - Enables 'lite' mode
 * -Prelocate=false - Adds 'no_relocate' modifier
 * -Prelease=true   - Removes commit/modifiers for release build
 *
 * Logic in: buildSrc/versioning/BuildConfig.kt & VersionUtil.kt
 */

import versioning.BuildConfig
import versioning.VersionUtil

BuildConfig.init(project)

val baseVersion = "2.3.74"
group = "ac.grim.grimac"
version = VersionUtil.computeVersion(project, baseVersion)
description = "Libre simulation anticheat designed for 26.2 with 1.8–26.2 support, powered by PacketEvents 2.0."

extra["timestamp"] = System.currentTimeMillis().toString()
extra["git_branch"] = VersionUtil.getGitBranch(project, true)
extra["git_commit"] = VersionUtil.getGitCommitHash(project, true)
extra["git_org"] = System.getenv("GRIM_GIT_ORG") ?: VersionUtil.getGitUser(project)
extra["git_repo"] = System.getenv("GRIM_GIT_REPO") ?: "Grim"

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

// ---------- Java Compile Optimization ----------
subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.isFork = true
        options.isIncremental = true
        options.encoding = "UTF-8"
    }
}
