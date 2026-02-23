plugins {
    kotlin("jvm") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.woxloi.custombiome"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    // -------------------------------------------------------
    // Kotlin stdlib / coroutines
    //
    // WoxloiDevAPI が既にサーバーへ Kotlin stdlib と
    // coroutines を提供している。
    // CustomBiome 側で implementation（JAR 同梱）にすると
    // 異なるクラスローダー上に同じクラスが2つ乗り、
    //   - ClassCastException
    //   - NoSuchMethodError
    // などの実行時競合が発生する。
    // compileOnly にしてコンパイル参照のみにし、
    // 実行時は WoxloiDevAPI が提供するものを使う。
    // -------------------------------------------------------
    compileOnly(kotlin("stdlib"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // -------------------------------------------------------
    // Minecraft API
    // -------------------------------------------------------
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    // -------------------------------------------------------
    // WoxloiDevAPI（libs/WoxloiDevAPI.jar を配置すること）
    // Kotlin / coroutines はこちらが提供するため重複しない。
    // -------------------------------------------------------
    compileOnly(files("libs/WoxloiDevAPI.jar"))

    // -------------------------------------------------------
    // WorldEdit / WorldGuard
    // -------------------------------------------------------
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9")

    // -------------------------------------------------------
    // Vault / PlaceholderAPI（softdepend）
    // -------------------------------------------------------
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.11.5")
}

kotlin {
    jvmToolchain(17)
}

// -------------------------------------------------------
// Shadow JAR
//
// 全依存が compileOnly のため Shadow JAR の中身は
// CustomBiome 自身のクラスのみになる（軽量 JAR）。
// relocate も不要（同梱するサードパーティがないため）。
// -------------------------------------------------------
tasks.shadowJar {
    archiveBaseName.set("CustomBiome")
    archiveClassifier.set("")
    archiveVersion.set(version.toString())

    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/NOTICE*")
    exclude("META-INF/LICENSE*")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.test {
    useJUnitPlatform()
}