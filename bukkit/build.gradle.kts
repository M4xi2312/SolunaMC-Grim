import net.minecrell.pluginyml.bukkit.BukkitPluginDescription.Permission
import versioning.BuildConfig

plugins {
    `maven-publish`
    grim.`base-conventions`
    grim.`shadow-conventions`
    id("de.eldoria.plugin-yml.bukkit") version "0.8.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    val localOverride = if (BuildConfig.mavenLocalOverride) mavenLocal() else null

    // PaperMC API & Build-Tools
    exclusiveContent {
        forRepository {
            maven("https://repo.papermc.io/repository/maven-public/") { name = "papermc" }
        }
        filter {
            includeGroup("io.papermc.paper")
            includeGroup("net.md-5")
        }
    }

    // Mojang interne Bibliotheken (z.B. Brigadier)
    exclusiveContent {
        forRepository {
            maven("https://libraries.minecraft.net") { mavenContent { releasesOnly() } }
        }
        filter {
            includeModule("com.mojang", "brigadier")
        }
    }

    // PlaceholderAPI
    exclusiveContent {
        forRepository {
            maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        }
        filter {
            includeGroup("me.clip")
        }
    }

    // LuckPerms API
    exclusiveContent {
        forRepository {
            maven("https://repo.luckperms.net/")
        }
        filter {
            includeGroup("net.luckperms")
        }
    }

    // GrimAC & PacketEvents Repositories (Zusammengefasst)
    val grimPublicReleases = maven("https://maven.grim.ac/public/releases") { mavenContent { releasesOnly() } }
    val grimPublicSnapshots = maven("https://maven.grim.ac/public/snapshots") { mavenContent { snapshotsOnly() } }
    val grimLegacySnapshots = maven("https://repo.grim.ac/snapshots")
    
    exclusiveContent {
        forRepositories(*listOfNotNull(localOverride, grimPublicReleases, grimPublicSnapshots, grimLegacySnapshots).toTypedArray())
        filter {
            includeGroup("ac.grim.grimac")
            includeGroup("com.github.retrooper")
        }
    }

    // Scarsz Repository (z.B. für Discordsrv / andere Brücken)
    exclusiveContent {
        forRepository {
            maven("https://nexus.scarsz.me/content/repositories/releases") { mavenContent { releasesOnly() } }
        }
        filter {
            includeGroup("github.scarsz")
        }
    }

    mavenCentral()
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.placeholderapi)
    compileOnly(libs.luckperms)

    // PacketEvents je nach Build-Konfiguration shaden oder nur bereitstellen
    val packetEventsConfig = if (BuildConfig.shadePE) "implementation" else "compileOnly"
    add(packetEventsConfig, libs.packetevents.spigot)

    implementation(libs.cloud.paper)
    implementation(libs.adventure.platform.bukkit)
    implementation(libs.grim.bukkit.internal)

    implementation(project(":common"))
    shadow(project(":common"))
}

bukkit {
    name = "GrimAC"
    author = "GrimAC"
    main = "ac.grim.grimac.platform.bukkit.GrimACBukkitLoaderPlugin"
    website = "https://grim.ac/"
    apiVersion = "1.13"
    foliaSupported = true

    if (!BuildConfig.shadePE) {
        depend = listOf("packetevents")
    }

    softDepend = listOf(
        "ProtocolLib",
        "ProtocolSupport",
        "Essentials",
        "ViaVersion",
        "ViaBackwards",
        "ViaRewind",
        "Geyser-Spigot",
        "floodgate",
        "FastLogin",
        "PlaceholderAPI",
        "LuckPerms",
        "sqlite-jdbc",
        "mysql-jdbc",
        "postgresql-jdbc",
        "mongodb-driver",
        "jedis"
    )

    permissions {
        register("grim.alerts") {
            description = "Receive alerts for violations"
            default = Permission.Default.OP
        }
        register("grim.alerts.enable-on-join") {
            description = "Enable alerts on join"
            default = Permission.Default.OP
        }
        register("grim.performance") {
            description = "Check performance metrics"
            default = Permission.Default.OP
        }
        register("grim.profile") {
            description = "Check user profile"
            default = Permission.Default.OP
        }
        register("grim.brand") {
            description = "Show client brands on join"
            default = Permission.Default.OP
        }
        register("grim.brand.enable-on-join") {
            description = "Enable showing client brands on join"
            default = Permission.Default.OP
        }
        register("grim.sendalert") {
            description = "Send cheater alert"
            default = Permission.Default.OP
        }
        register("grim.nosetback") {
            description = "Disable setback"
            default = Permission.Default.FALSE
        }
        register("grim.nomodifypacket") {
            description = "Disable modifying packets"
            default = Permission.Default.FALSE
        }
        register("grim.disabled") {
            description = "Disable Grim checks while keeping player state tracked"
            default = Permission.Default.FALSE
        }
        register("grim.exempt") {
            description = "Exempt from all checks"
            default = Permission.Default.FALSE
        }
        register("grim.verbose") {
            description = "Receive verbose alerts for violations"
            default = Permission.Default.OP
        }
        register("grim.verbose.enable-on-join") {
            description = "Enable verbose alerts on join"
            default = Permission.Default.FALSE
        }
        register("grim.list") {
            description = "Shows lists of specific data"
            default = Permission.Default.FALSE
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.named("shadowJar"))
        }
    }
}

tasks {
    val version = "26.2"
    val javaVersion = JavaLanguageVersion.of(25)

    val jvmArgsExternal = listOf(
        "-Dcom.mojang.eula.agree=true",
        "-Dpaper.explicit-flush=true",
        "-DPaper.IgnoreJavaVersion=true"
    )

    runServer {
        minecraftVersion(version)
        runDirectory = projectDir.resolve("run/$version")

        val javaToolchains = project.extensions.getByType<JavaToolchainService>()
        javaLauncher = javaToolchains.launcherFor {
            vendor = JvmVendorSpec.JETBRAINS
            languageVersion = javaVersion
        }

        jvmArgs = jvmArgsExternal
    }

    shadowJar {
        exclude("META-INF/services/javax.annotation.processing.Processor")

        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
    }
}
