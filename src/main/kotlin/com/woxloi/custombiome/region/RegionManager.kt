package com.woxloi.custombiome.region

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.woxloi.custombiome.api.BiomeRegistry
import com.woxloi.custombiome.biome.CustomBiome
import com.woxloi.custombiome.database.BiomeDatabase
import com.woxloi.custombiome.utils.Logger
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

object RegionManager {

    private val regions = ConcurrentHashMap<String, BiomeRegion>()

    private lateinit var database: BiomeDatabase
    private var autoCreateRegion: Boolean = true
    private var regionPrefix: String = "cb_"

    fun init(db: BiomeDatabase, autoCreate: Boolean, prefix: String) {
        database         = db
        autoCreateRegion = autoCreate
        regionPrefix     = prefix
        loadFromDatabase()
    }

    fun assignBiomeToSelection(player: Player, biome: CustomBiome): Boolean {
        val wePlayer  = BukkitAdapter.adapt(player)
        val session   = WorldEdit.getInstance().sessionManager.get(wePlayer)
        val selection = session.selectionWorld?.let {
            runCatching { session.getSelection(it) }.getOrNull()
        }

        if (selection == null) {
            Logger.warn("${player.name} has no WorldEdit selection.")
            return false
        }

        val world     = player.world
        val regionId  = "$regionPrefix${biome.key}_${System.currentTimeMillis() % 10000}"
        val container = WorldGuard.getInstance().platform.regionContainer
        val wgManager = container.get(BukkitAdapter.adapt(world))

        if (wgManager == null) {
            Logger.error("WorldGuard RegionManager is unavailable for world '${world.name}'.")
            return false
        }

        val min    = selection.minimumPoint
        val max    = selection.maximumPoint
        val region = ProtectedCuboidRegion(
            regionId,
            BlockVector3.at(min.blockX, min.blockY, min.blockZ),
            BlockVector3.at(max.blockX, max.blockY, max.blockZ)
        )
        wgManager.addRegion(region)

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

    fun assignBiomeToRegion(regionId: String, worldName: String, biome: CustomBiome, assignedBy: String): Boolean {
        val world     = Bukkit.getWorld(worldName) ?: return false
        val container = WorldGuard.getInstance().platform.regionContainer
        val wgManager = container.get(BukkitAdapter.adapt(world)) ?: return false

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

    /** ワンド選択などで直接登録する場合（WGリージョンはすでに作成済み）*/
    fun registerDirect(biomeRegion: BiomeRegion) {
        regions[biomeRegion.regionId] = biomeRegion
        database.saveRegion(
            biomeRegion.regionId,
            biomeRegion.worldName,
            biomeRegion.biome.key,
            biomeRegion.assignedBy
        )
        Logger.success("Registered biome region '${biomeRegion.regionId}' directly.")
    }

    fun getBiomeAt(location: Location): CustomBiome? {
        val world     = location.world ?: return null
        val container = WorldGuard.getInstance().platform.regionContainer
        val wgManager = container.get(BukkitAdapter.adapt(world)) ?: return null

        // Y 軸込みの座標で適用リージョンを取得
        val pos        = BlockVector3.at(location.blockX, location.blockY, location.blockZ)
        val applicable = wgManager.getApplicableRegions(pos)

        // 複数リージョンが重なった場合は体積が最小（＝最も具体的）なリージョンを優先する
        // 体積 = (maxX-minX+1) * (maxY-minY+1) * (maxZ-minZ+1)
        var bestRegion: BiomeRegion? = null
        var bestVolume = Long.MAX_VALUE

        for (wgRegion in applicable) {
            val biomeRegion = regions[wgRegion.id] ?: continue
            val min = wgRegion.minimumPoint
            val max = wgRegion.maximumPoint
            val volume = (max.x - min.x + 1).toLong() *
                    (max.y - min.y + 1).toLong() *
                    (max.z - min.z + 1).toLong()
            if (volume < bestVolume) {
                bestVolume = volume
                bestRegion = biomeRegion
            }
        }

        return bestRegion?.biome
    }

    fun getRegion(regionId: String): BiomeRegion? = regions[regionId]
    fun getAllRegions(): List<BiomeRegion> = regions.values.toList()
    fun getRegionsByBiome(biomeKey: String): List<BiomeRegion> = regions.values.filter { it.biome.key == biomeKey }

    fun removeRegion(regionId: String, removeFromWG: Boolean = false): Boolean {
        val biomeRegion = regions.remove(regionId) ?: return false
        database.deleteRegion(regionId)
        if (removeFromWG) {
            val world     = Bukkit.getWorld(biomeRegion.worldName) ?: return true
            val container = WorldGuard.getInstance().platform.regionContainer
            val wgManager = container.get(BukkitAdapter.adapt(world)) ?: return true
            wgManager.removeRegion(regionId)
        }
        Logger.info("Removed biome region '$regionId'.")
        return true
    }

    private fun loadFromDatabase() {
        val records = database.loadAllRegions()
        var count = 0
        for (record in records) {
            val regionId   = record["region_id"]   as? String ?: continue
            val worldName  = record["world_name"]  as? String ?: continue
            val biomeKey   = record["biome_key"]   as? String ?: continue
            val assignedBy = record["assigned_by"] as? String ?: "unknown"
            val assignedAt = (record["assigned_at"] as? Number)?.toLong() ?: 0L
            val biome      = BiomeRegistry.get(biomeKey) ?: continue
            regions[regionId] = BiomeRegion(regionId, worldName, biome, assignedBy, assignedAt)
            count++
        }
        Logger.info("Loaded $count biome region(s) from database.")
    }
}