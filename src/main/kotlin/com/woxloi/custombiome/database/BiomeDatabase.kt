package com.woxloi.custombiome.database

import com.woxloi.custombiome.utils.Logger
import com.woxloi.custombiome.world.CustomWorld

class BiomeDatabase(private val provider: MySQLProvider) {

    fun init() { createTables(); Logger.success("BiomeDatabase tables ready.") }
    fun close() { provider.close() }

    private fun createTables() {
        provider.getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS cb_worlds (
                        id          INT AUTO_INCREMENT PRIMARY KEY,
                        world_name  VARCHAR(64) NOT NULL UNIQUE,
                        biome_key   VARCHAR(64) NOT NULL,
                        seed        BIGINT NOT NULL,
                        created_by  VARCHAR(36) NOT NULL,
                        created_at  BIGINT NOT NULL
                    ) CHARACTER SET utf8mb4
                """.trimIndent())
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS cb_visits (
                        id          INT AUTO_INCREMENT PRIMARY KEY,
                        uuid        VARCHAR(36) NOT NULL,
                        biome_key   VARCHAR(64) NOT NULL,
                        world_name  VARCHAR(64) NOT NULL,
                        visited_at  BIGINT NOT NULL
                    ) CHARACTER SET utf8mb4
                """.trimIndent())
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS cb_regions (
                        id          INT AUTO_INCREMENT PRIMARY KEY,
                        region_id   VARCHAR(128) NOT NULL UNIQUE,
                        world_name  VARCHAR(64) NOT NULL,
                        biome_key   VARCHAR(64) NOT NULL,
                        assigned_by VARCHAR(36) NOT NULL,
                        assigned_at BIGINT NOT NULL
                    ) CHARACTER SET utf8mb4
                """.trimIndent())
            }
        }
    }

    fun saveWorld(world: CustomWorld) {
        runCatching {
            provider.getConnection().use { conn ->
                conn.prepareStatement("""
                    INSERT INTO cb_worlds (world_name, biome_key, seed, created_by, created_at)
                    VALUES (?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE biome_key=VALUES(biome_key), seed=VALUES(seed)
                """.trimIndent()).use { ps ->
                    ps.setString(1, world.worldName); ps.setString(2, world.biome.key)
                    ps.setLong(3, world.seed); ps.setString(4, world.createdBy); ps.setLong(5, world.createdAt)
                    ps.executeUpdate()
                }
            }
        }.onFailure { Logger.error("Failed to save world '${world.worldName}': ${it.message}") }
    }

    fun deleteWorld(worldName: String) {
        runCatching {
            provider.getConnection().use { conn ->
                conn.prepareStatement("DELETE FROM cb_worlds WHERE world_name = ?").use { ps ->
                    ps.setString(1, worldName); ps.executeUpdate()
                }
            }
        }.onFailure { Logger.error("Failed to delete world '$worldName': ${it.message}") }
    }

    fun loadAllWorlds(): List<Map<String, Any>> {
        return runCatching {
            provider.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    val rs = stmt.executeQuery("SELECT * FROM cb_worlds")
                    val result = mutableListOf<Map<String, Any>>()
                    while (rs.next()) {
                        result += mapOf(
                            "world_name" to rs.getString("world_name"),
                            "biome_key"  to rs.getString("biome_key"),
                            "seed"       to rs.getLong("seed"),
                            "created_by" to rs.getString("created_by"),
                            "created_at" to rs.getLong("created_at")
                        )
                    }
                    result
                }
            }
        }.getOrElse { Logger.error("Failed to load worlds: ${it.message}"); emptyList() }
    }

    fun recordVisit(uuid: String, biomeKey: String, worldName: String) {
        runCatching {
            provider.getConnection().use { conn ->
                conn.prepareStatement(
                    "INSERT INTO cb_visits (uuid, biome_key, world_name, visited_at) VALUES (?, ?, ?, ?)"
                ).use { ps ->
                    ps.setString(1, uuid); ps.setString(2, biomeKey)
                    ps.setString(3, worldName); ps.setLong(4, System.currentTimeMillis() / 1000L)
                    ps.executeUpdate()
                }
            }
        }.onFailure { Logger.error("Failed to record visit: ${it.message}") }
    }

    fun getVisitHistory(uuid: String): List<Map<String, Any>> {
        return runCatching {
            provider.getConnection().use { conn ->
                conn.prepareStatement("SELECT * FROM cb_visits WHERE uuid = ? ORDER BY visited_at DESC").use { ps ->
                    ps.setString(1, uuid)
                    val rs = ps.executeQuery()
                    val result = mutableListOf<Map<String, Any>>()
                    while (rs.next()) {
                        result += mapOf(
                            "uuid"       to rs.getString("uuid"),
                            "biome_key"  to rs.getString("biome_key"),
                            "world_name" to rs.getString("world_name"),
                            "visited_at" to rs.getLong("visited_at")
                        )
                    }
                    result
                }
            }
        }.getOrElse { emptyList() }
    }

    fun saveRegion(regionId: String, worldName: String, biomeKey: String, assignedBy: String) {
        runCatching {
            provider.getConnection().use { conn ->
                conn.prepareStatement("""
                    INSERT INTO cb_regions (region_id, world_name, biome_key, assigned_by, assigned_at)
                    VALUES (?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE biome_key=VALUES(biome_key), assigned_by=VALUES(assigned_by)
                """.trimIndent()).use { ps ->
                    ps.setString(1, regionId); ps.setString(2, worldName); ps.setString(3, biomeKey)
                    ps.setString(4, assignedBy); ps.setLong(5, System.currentTimeMillis() / 1000L)
                    ps.executeUpdate()
                }
            }
        }.onFailure { Logger.error("Failed to save region '$regionId': ${it.message}") }
    }

    fun deleteRegion(regionId: String) {
        runCatching {
            provider.getConnection().use { conn ->
                conn.prepareStatement("DELETE FROM cb_regions WHERE region_id = ?").use { ps ->
                    ps.setString(1, regionId); ps.executeUpdate()
                }
            }
        }.onFailure { Logger.error("Failed to delete region '$regionId': ${it.message}") }
    }

    fun loadAllRegions(): List<Map<String, Any>> {
        return runCatching {
            provider.getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    val rs = stmt.executeQuery("SELECT * FROM cb_regions")
                    val result = mutableListOf<Map<String, Any>>()
                    while (rs.next()) {
                        result += mapOf(
                            "region_id"   to rs.getString("region_id"),
                            "world_name"  to rs.getString("world_name"),
                            "biome_key"   to rs.getString("biome_key"),
                            "assigned_by" to rs.getString("assigned_by"),
                            "assigned_at" to rs.getLong("assigned_at")
                        )
                    }
                    result
                }
            }
        }.getOrElse { emptyList() }
    }
}
