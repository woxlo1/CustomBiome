package com.woxloi.custombiome.database

import com.woxloi.custombiome.utils.Logger
import com.woxloi.custombiome.world.CustomWorld
import com.woxloi.devapi.database.DatabaseRegistry
import com.woxloi.devapi.database.DatabaseTable
import com.woxloi.devapi.database.sql.MySQLProvider

/**
 * CustomBiome の MySQL 永続化レイヤー。
 * WoxloiDevAPI の DatabaseRegistry / DatabaseTable を使ってクエリを実行する。
 *
 * テーブル:
 *   cb_worlds   … 生成ワールド情報
 *   cb_visits   … プレイヤーのバイオーム訪問履歴
 *   cb_regions  … WG リージョン割り当て情報
 */
class BiomeDatabase(private val provider: MySQLProvider) {

    private lateinit var worldTable:  DatabaseTable
    private lateinit var visitTable:  DatabaseTable
    private lateinit var regionTable: DatabaseTable

    fun init() {
        worldTable  = DatabaseRegistry.registerTable("cb_worlds",  provider)
        visitTable  = DatabaseRegistry.registerTable("cb_visits",  provider)
        regionTable = DatabaseRegistry.registerTable("cb_regions", provider)

        createTables()
        Logger.success("BiomeDatabase tables ready.")
    }

    private fun createTables() {
        worldTable.create(mapOf(
            "id"         to "INT AUTO_INCREMENT PRIMARY KEY",
            "world_name" to "VARCHAR(64) NOT NULL UNIQUE",
            "biome_key"  to "VARCHAR(64) NOT NULL",
            "seed"       to "BIGINT NOT NULL",
            "created_by" to "VARCHAR(36) NOT NULL",
            "created_at" to "BIGINT NOT NULL"
        ))

        visitTable.create(mapOf(
            "id"         to "INT AUTO_INCREMENT PRIMARY KEY",
            "uuid"       to "VARCHAR(36) NOT NULL",
            "biome_key"  to "VARCHAR(64) NOT NULL",
            "world_name" to "VARCHAR(64) NOT NULL",
            "visited_at" to "BIGINT NOT NULL"
        ))

        regionTable.create(mapOf(
            "id"          to "INT AUTO_INCREMENT PRIMARY KEY",
            "region_id"   to "VARCHAR(128) NOT NULL UNIQUE",
            "world_name"  to "VARCHAR(64) NOT NULL",
            "biome_key"   to "VARCHAR(64) NOT NULL",
            "assigned_by" to "VARCHAR(36) NOT NULL",
            "assigned_at" to "BIGINT NOT NULL"
        ))
    }

    // ----------------------------------------------------------------
    // ワールド CRUD
    // ----------------------------------------------------------------

    fun saveWorld(world: CustomWorld) {
        runCatching {
            worldTable.insert(world.toDbMap())
        }.onFailure {
            Logger.error("Failed to save world '${world.worldName}': ${it.message}")
        }
    }

    fun deleteWorld(worldName: String) {
        runCatching {
            worldTable.delete("world_name", worldName)
        }.onFailure {
            Logger.error("Failed to delete world '$worldName': ${it.message}")
        }
    }

    fun loadAllWorlds(): List<Map<String, Any>> {
        return runCatching {
            worldTable.findAll()
        }.getOrElse {
            Logger.error("Failed to load worlds: ${it.message}")
            emptyList()
        }
    }

    // ----------------------------------------------------------------
    // 訪問履歴
    // ----------------------------------------------------------------

    fun recordVisit(uuid: String, biomeKey: String, worldName: String) {
        runCatching {
            visitTable.insert(mapOf(
                "uuid"       to uuid,
                "biome_key"  to biomeKey,
                "world_name" to worldName,
                "visited_at" to System.currentTimeMillis() / 1000L
            ))
        }.onFailure {
            Logger.error("Failed to record visit: ${it.message}")
        }
    }

    fun getVisitHistory(uuid: String): List<Map<String, Any>> {
        return runCatching {
            visitTable.find("uuid", uuid)?.let { listOf(it) } ?: emptyList()
        }.getOrElse { emptyList() }
    }

    // ----------------------------------------------------------------
    // リージョン割り当て
    // ----------------------------------------------------------------

    fun saveRegion(regionId: String, worldName: String, biomeKey: String, assignedBy: String) {
        runCatching {
            regionTable.insert(mapOf(
                "region_id"   to regionId,
                "world_name"  to worldName,
                "biome_key"   to biomeKey,
                "assigned_by" to assignedBy,
                "assigned_at" to System.currentTimeMillis() / 1000L
            ))
        }.onFailure {
            Logger.error("Failed to save region '$regionId': ${it.message}")
        }
    }

    fun deleteRegion(regionId: String) {
        runCatching {
            regionTable.delete("region_id", regionId)
        }.onFailure {
            Logger.error("Failed to delete region '$regionId': ${it.message}")
        }
    }

    fun loadAllRegions(): List<Map<String, Any>> {
        return runCatching {
            regionTable.findAll()
        }.getOrElse { emptyList() }
    }
}
