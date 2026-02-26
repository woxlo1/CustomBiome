package com.woxloi.custombiome.database

import com.woxloi.custombiome.utils.Logger
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

/**
 * MySQL が使えない場合のフォールバック。
 * worlds / regions / visits を YAML ファイルに保存する。
 */
class FlatFileProvider(private val dataFolder: File) {

    private val worldsFile  = File(dataFolder, "data/worlds.yml")
    private val regionsFile = File(dataFolder, "data/regions.yml")
    private val visitsFile  = File(dataFolder, "data/visits.yml")

    fun init() {
        dataFolder.resolve("data").mkdirs()
        listOf(worldsFile, regionsFile, visitsFile).forEach { f ->
            if (!f.exists()) f.createNewFile()
        }
        Logger.warn("Running in FLAT-FILE mode. MySQL is not available.")
    }

    // ── Worlds ───────────────────────────────────────────────────────────

    fun saveWorld(worldName: String, biomeKey: String, seed: Long, createdBy: String, createdAt: Long) {
        val cfg = YamlConfiguration.loadConfiguration(worldsFile)
        cfg.set("$worldName.biome_key",   biomeKey)
        cfg.set("$worldName.seed",        seed)
        cfg.set("$worldName.created_by",  createdBy)
        cfg.set("$worldName.created_at",  createdAt)
        cfg.save(worldsFile)
    }

    fun deleteWorld(worldName: String) {
        val cfg = YamlConfiguration.loadConfiguration(worldsFile)
        cfg.set(worldName, null)
        cfg.save(worldsFile)
    }

    fun loadAllWorlds(): List<Map<String, Any>> {
        val cfg = YamlConfiguration.loadConfiguration(worldsFile)
        return cfg.getKeys(false).mapNotNull { key ->
            val sec = cfg.getConfigurationSection(key) ?: return@mapNotNull null
            mapOf(
                "world_name" to key,
                "biome_key"  to (sec.getString("biome_key") ?: return@mapNotNull null),
                "seed"       to sec.getLong("seed"),
                "created_by" to (sec.getString("created_by") ?: "unknown"),
                "created_at" to sec.getLong("created_at")
            )
        }
    }

    // ── Regions ──────────────────────────────────────────────────────────

    fun saveRegion(regionId: String, worldName: String, biomeKey: String, assignedBy: String, assignedAt: Long) {
        val cfg = YamlConfiguration.loadConfiguration(regionsFile)
        cfg.set("$regionId.world_name",  worldName)
        cfg.set("$regionId.biome_key",   biomeKey)
        cfg.set("$regionId.assigned_by", assignedBy)
        cfg.set("$regionId.assigned_at", assignedAt)
        cfg.save(regionsFile)
    }

    fun deleteRegion(regionId: String) {
        val cfg = YamlConfiguration.loadConfiguration(regionsFile)
        cfg.set(regionId, null)
        cfg.save(regionsFile)
    }

    fun loadAllRegions(): List<Map<String, Any>> {
        val cfg = YamlConfiguration.loadConfiguration(regionsFile)
        return cfg.getKeys(false).mapNotNull { key ->
            val sec = cfg.getConfigurationSection(key) ?: return@mapNotNull null
            mapOf(
                "region_id"   to key,
                "world_name"  to (sec.getString("world_name") ?: return@mapNotNull null),
                "biome_key"   to (sec.getString("biome_key")  ?: return@mapNotNull null),
                "assigned_by" to (sec.getString("assigned_by") ?: "unknown"),
                "assigned_at" to sec.getLong("assigned_at")
            )
        }
    }

    // ── Visits ───────────────────────────────────────────────────────────

    fun recordVisit(uuid: String, biomeKey: String, worldName: String, visitedAt: Long) {
        val cfg  = YamlConfiguration.loadConfiguration(visitsFile)
        val list = cfg.getMapList(uuid).toMutableList()
        list.add(mapOf("biome_key" to biomeKey, "world_name" to worldName, "visited_at" to visitedAt))
        // 直近 100 件だけ保持
        val trimmed = if (list.size > 100) list.takeLast(100) else list
        cfg.set(uuid, trimmed)
        cfg.save(visitsFile)
    }

    fun getVisitHistory(uuid: String): List<Map<String, Any>> {
        val cfg = YamlConfiguration.loadConfiguration(visitsFile)
        @Suppress("UNCHECKED_CAST")
        return (cfg.getMapList(uuid) as List<Map<String, Any>>).reversed()
    }
}
