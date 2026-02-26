package com.woxloi.custombiome.database

import com.woxloi.custombiome.utils.Logger
import com.woxloi.custombiome.world.CustomWorld
import java.io.File

/**
 * MySQL が利用可能なら MySQLProvider を使い、
 * 接続に失敗した場合は FlatFileProvider にフォールバックする。
 * プラグインが MySQL なしでも起動できるようにする。
 *
 * MySQL 側の全 DB 操作は MySQLProvider.withConnection {} を通じて行う。
 * withConnection は接続取得失敗を内部で吸収し null を返すため、
 * 呼び出し側は ?.let / ?: で安全に処理できる。
 */
class BiomeDatabase private constructor(
    private val mysql: MySQLProvider?,
    private val flatFile: FlatFileProvider?
) {

    companion object {
        fun create(provider: MySQLProvider, dataFolder: File): BiomeDatabase {
            return if (provider.connect()) {
                Logger.success("MySQL connected for CustomBiome.")
                BiomeDatabase(mysql = provider, flatFile = null)
            } else {
                Logger.warn("MySQL connection failed. Falling back to flat-file storage.")
                val ff = FlatFileProvider(dataFolder).also { it.init() }
                BiomeDatabase(mysql = null, flatFile = ff)
            }
        }
    }

    val isMySql: Boolean get() = mysql != null

    fun init() {
        if (mysql != null) { createMySQLTables(); Logger.success("BiomeDatabase (MySQL) tables ready.") }
    }
    fun close() { mysql?.close() }

    // ── worlds ───────────────────────────────────────────────────────────

    fun saveWorld(world: CustomWorld) {
        if (mysql != null) {
            val ok = mysql.withConnection { conn ->
                conn.prepareStatement("""
                    INSERT INTO cb_worlds (world_name, biome_key, seed, created_by, created_at)
                    VALUES (?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE biome_key=VALUES(biome_key), seed=VALUES(seed)
                """.trimIndent()).use { ps ->
                    ps.setString(1, world.worldName); ps.setString(2, world.biome.key)
                    ps.setLong(3, world.seed); ps.setString(4, world.createdBy)
                    ps.setLong(5, world.createdAt); ps.executeUpdate()
                }
            }
            if (ok == null) Logger.error("Failed to save world '${world.worldName}': connection unavailable")
        } else {
            flatFile!!.saveWorld(world.worldName, world.biome.key, world.seed, world.createdBy, world.createdAt)
        }
    }

    fun deleteWorld(worldName: String) {
        if (mysql != null) {
            val ok = mysql.withConnection { conn ->
                conn.prepareStatement("DELETE FROM cb_worlds WHERE world_name = ?").use { ps ->
                    ps.setString(1, worldName); ps.executeUpdate()
                }
            }
            if (ok == null) Logger.error("Failed to delete world '$worldName': connection unavailable")
        } else {
            flatFile!!.deleteWorld(worldName)
        }
    }

    fun loadAllWorlds(): List<Map<String, Any>> {
        if (mysql != null) {
            return mysql.withConnection { conn ->
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
            } ?: run {
                Logger.error("Failed to load worlds: connection unavailable"); emptyList()
            }
        } else {
            return flatFile!!.loadAllWorlds()
        }
    }

    // ── visits ───────────────────────────────────────────────────────────

    fun recordVisit(uuid: String, biomeKey: String, worldName: String) {
        val now = System.currentTimeMillis() / 1000L
        if (mysql != null) {
            val ok = mysql.withConnection { conn ->
                conn.prepareStatement(
                    "INSERT INTO cb_visits (uuid, biome_key, world_name, visited_at) VALUES (?, ?, ?, ?)"
                ).use { ps ->
                    ps.setString(1, uuid); ps.setString(2, biomeKey)
                    ps.setString(3, worldName); ps.setLong(4, now); ps.executeUpdate()
                }
            }
            if (ok == null) Logger.error("Failed to record visit: connection unavailable")
        } else {
            flatFile!!.recordVisit(uuid, biomeKey, worldName, now)
        }
    }

    fun getVisitHistory(uuid: String): List<Map<String, Any>> {
        if (mysql != null) {
            return mysql.withConnection { conn ->
                conn.prepareStatement(
                    "SELECT * FROM cb_visits WHERE uuid = ? ORDER BY visited_at DESC"
                ).use { ps ->
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
            } ?: emptyList()
        } else {
            return flatFile!!.getVisitHistory(uuid)
        }
    }

    // ── regions ──────────────────────────────────────────────────────────

    fun saveRegion(regionId: String, worldName: String, biomeKey: String, assignedBy: String) {
        val now = System.currentTimeMillis() / 1000L
        if (mysql != null) {
            val ok = mysql.withConnection { conn ->
                conn.prepareStatement("""
                    INSERT INTO cb_regions (region_id, world_name, biome_key, assigned_by, assigned_at)
                    VALUES (?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE biome_key=VALUES(biome_key), assigned_by=VALUES(assigned_by)
                """.trimIndent()).use { ps ->
                    ps.setString(1, regionId); ps.setString(2, worldName); ps.setString(3, biomeKey)
                    ps.setString(4, assignedBy); ps.setLong(5, now); ps.executeUpdate()
                }
            }
            if (ok == null) Logger.error("Failed to save region '$regionId': connection unavailable")
        } else {
            flatFile!!.saveRegion(regionId, worldName, biomeKey, assignedBy, now)
        }
    }

    fun deleteRegion(regionId: String) {
        if (mysql != null) {
            val ok = mysql.withConnection { conn ->
                conn.prepareStatement("DELETE FROM cb_regions WHERE region_id = ?").use { ps ->
                    ps.setString(1, regionId); ps.executeUpdate()
                }
            }
            if (ok == null) Logger.error("Failed to delete region '$regionId': connection unavailable")
        } else {
            flatFile!!.deleteRegion(regionId)
        }
    }

    fun loadAllRegions(): List<Map<String, Any>> {
        if (mysql != null) {
            return mysql.withConnection { conn ->
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
            } ?: emptyList()
        } else {
            return flatFile!!.loadAllRegions()
        }
    }

    // ── private ──────────────────────────────────────────────────────────

    private fun createMySQLTables() {
        // init() からのみ呼ばれ、mysql != null が保証されている
        mysql?.withConnection { conn ->
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
        } ?: Logger.error("createMySQLTables: connection unavailable")
    }
}
