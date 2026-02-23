package com.woxloi.custombiome.region

import com.woxloi.custombiome.api.BiomeRegistry
import com.woxloi.custombiome.biome.CustomBiome
import com.woxloi.custombiome.database.BiomeDatabase
import com.woxloi.devapi.hooks.world.WorldGuardAPI
import com.woxloi.devapi.hooks.world.WorldEditAPI
import com.woxloi.devapi.utils.Logger
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.managers.RegionManager
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldedit.math.BlockVector3
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

/**
 * WorldGuard リージョンとカスタムバイオームの紐付けを管理する。
 * WoxloiDevAPI の WorldGuardAPI / WorldEditAPI フックを活用する。
 */
object RegionManager {

    /** regionId → BiomeRegion のメモリキャッシュ */
    private val regions = ConcurrentHashMap<String, BiomeRegion>()

    private lateinit var database: BiomeDatabase
    private var autoCreateRegion: Boolean = true
    private var regionPrefix: String = "cb_"

    fun init(db: BiomeDatabase, autoCreate: Boolean, prefix: String) {
        database     = db
        autoCreateRegion = autoCreate
        regionPrefix = prefix
        loadFromDatabase()
    }

    // ----------------------------------------------------------------
    // プレイヤーの WorldEdit 選択範囲にバイオームを割り当て
    // ----------------------------------------------------------------

    /**
     * プレイヤーの WorldEdit 選択をもとに WG リージョンを作成（または既存を使用）し、
     * バイオームを割り当てる。
     * @return 成功したら true
     */
    fun assignBiomeToSelection(player: Player, biome: CustomBiome): Boolean {
        val selection = WorldEditAPI.getSelection(player)
        if (selection == null) {
            Logger.warn("${player.name} has no WorldEdit selection.")
            return false
        }

        val world      = player.world
        val regionId   = "$regionPrefix${biome.key}_${System.currentTimeMillis() % 10000}"
        val wgManager  = WorldGuardAPI.getRegionManager(world) ?: run {
            Logger.error("WorldGuard RegionManager is unavailable for world '${world.name}'.")
            return false
        }

        // WG リージョン作成
        val min = selection.minimumPoint
        val max = selection.maximumPoint
        val region = ProtectedCuboidRegion(
            regionId,
            BlockVector3.at(min.blockX, min.blockY, min.blockZ),
            BlockVector3.at(max.blockX, max.blockY, max.blockZ)
        )
        wgManager.addRegion(region)

        // メモリ・DB に保存
        val biomeRegion = BiomeRegion(
            regionId   = regionId,
            worldName  = world.name,
            biome      = biome,
            assignedBy = player.uniqueId.toString()
        )
        regions[regionId] = biomeRegion
        database.saveRegion(regionId, world.name, biome.key, player.uniqueId.toString())

        Logger.success("Assigned biome '${biome.key}' to WG region '$regionId' in '${world.name}'.")
        return true
    }

    /**
     * 既存の WG リージョン ID にバイオームを割り当てる。
     */
    fun assignBiomeToRegion(
        regionId: String,
        worldName: String,
        biome: CustomBiome,
        assignedBy: String
    ): Boolean {
        val world     = org.bukkit.Bukkit.getWorld(worldName) ?: return false
        val wgManager = WorldGuardAPI.getRegionManager(world) ?: return false
        if (wgManager.getRegion(regionId) == null) {
            Logger.warn("WG region '$regionId' does not exist in world '$worldName'.")
            return false
        }

        val biomeRegion = BiomeRegion(regionId, worldName, biome, assignedBy)
        regions[regionId] = biomeRegion
        database.saveRegion(regionId, worldName, biome.key, assignedBy)
        Logger.success("Assigned biome '${biome.key}' to existing WG region '$regionId'.")
        return true
    }

    // ----------------------------------------------------------------
    // 取得
    // ----------------------------------------------------------------

    /** 指定座標のバイオームを返す（WG リージョンから逆引き） */
    fun getBiomeAt(location: Location): CustomBiome? {
        val wgRegions = WorldGuardAPI.getRegionsAt(location)
        for (wgRegion in wgRegions) {
            val biomeRegion = regions[wgRegion.id] ?: continue
            return biomeRegion.biome
        }
        return null
    }

    fun getRegion(regionId: String): BiomeRegion? = regions[regionId]

    fun getAllRegions(): List<BiomeRegion> = regions.values.toList()

    fun getRegionsByBiome(biomeKey: String): List<BiomeRegion> =
        regions.values.filter { it.biome.key == biomeKey }

    // ----------------------------------------------------------------
    // 削除
    // ----------------------------------------------------------------

    fun removeRegion(regionId: String, removeFromWG: Boolean = false): Boolean {
        val biomeRegion = regions.remove(regionId) ?: return false
        database.deleteRegion(regionId)

        if (removeFromWG) {
            val world     = org.bukkit.Bukkit.getWorld(biomeRegion.worldName) ?: return true
            val wgManager = WorldGuardAPI.getRegionManager(world) ?: return true
            wgManager.removeRegion(regionId)
        }

        Logger.info("Removed biome region '$regionId'.")
        return true
    }

    // ----------------------------------------------------------------
    // DB 復元
    // ----------------------------------------------------------------

    private fun loadFromDatabase() {
        val records = database.loadAllRegions()
        var count = 0
        for (record in records) {
            val regionId  = record["region_id"]  as? String ?: continue
            val worldName = record["world_name"] as? String ?: continue
            val biomeKey  = record["biome_key"]  as? String ?: continue
            val assignedBy = record["assigned_by"] as? String ?: "unknown"
            val assignedAt = (record["assigned_at"] as? Number)?.toLong() ?: 0L

            val biome = BiomeRegistry.get(biomeKey) ?: continue

            regions[regionId] = BiomeRegion(regionId, worldName, biome, assignedBy, assignedAt)
            count++
        }
        Logger.info("Loaded $count biome region(s) from database.")
    }
}
