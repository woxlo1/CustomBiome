package com.woxloi.custombiome.api

import com.woxloi.custombiome.biome.CustomBiome
import com.woxloi.custombiome.region.RegionManager
import com.woxloi.custombiome.world.CustomWorld
import com.woxloi.custombiome.world.WorldManager
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

object BiomeAPI {
    fun getBiome(key: String): CustomBiome? = BiomeRegistry.get(key)
    fun getAllBiomes(): List<CustomBiome> = BiomeRegistry.getAll()
    fun getBiomeCount(): Int = BiomeRegistry.count()
    fun createWorld(biome: CustomBiome, worldName: String? = null, seed: Long = 0L, createdBy: String = "api"): World? =
        WorldManager.createWorld(biome, worldName, seed, createdBy)
    fun getCustomWorld(worldName: String): CustomWorld? = WorldManager.getCustomWorld(worldName)
    fun getAllCustomWorlds(): List<CustomWorld> = WorldManager.getAllWorlds()
    fun isCustomWorld(worldName: String): Boolean = WorldManager.isCustomWorld(worldName)
    fun assignBiomeToRegion(player: Player, biome: CustomBiome): Boolean =
        RegionManager.assignBiomeToSelection(player, biome)
    fun getBiomeAt(location: Location): CustomBiome? = RegionManager.getBiomeAt(location)
}
