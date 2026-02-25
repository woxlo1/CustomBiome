package com.woxloi.custombiome.world

import com.woxloi.custombiome.biome.CustomBiome
import com.woxloi.custombiome.database.BiomeDatabase
import com.woxloi.custombiome.generator.CustomChunkGenerator
import com.woxloi.custombiome.utils.Logger
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import java.util.concurrent.ConcurrentHashMap

object WorldManager {

    private val worlds = ConcurrentHashMap<String, CustomWorld>()
    private lateinit var database: BiomeDatabase
    private var worldPrefix: String = "cb_"

    fun init(db: BiomeDatabase, prefix: String) {
        database = db; worldPrefix = prefix; loadFromDatabase()
    }

    fun createWorld(biome: CustomBiome, worldName: String? = null, seed: Long = 0L, createdBy: String = "console"): World? {
        val name      = worldName ?: "$worldPrefix${biome.key}_${System.currentTimeMillis() % 10000}"
        val finalSeed = if (seed == 0L) java.util.Random().nextLong() else seed

        if (Bukkit.getWorld(name) != null) {
            Logger.warn("World '$name' already exists.")
            return Bukkit.getWorld(name)
        }

        Logger.info("Generating custom world '$name' with biome '${biome.key}' (seed=$finalSeed)...")
        val generator = CustomChunkGenerator(biome, finalSeed)
        val creator   = WorldCreator(name).generator(generator).generateStructures(false).seed(finalSeed).type(WorldType.FLAT)

        val world = runCatching { creator.createWorld() }.getOrElse { e ->
            Logger.error("Failed to create world '$name': ${e.message}"); e.printStackTrace(); return null
        } ?: return null

        val customWorld = CustomWorld(name, biome, finalSeed, createdBy, generator = generator)
        worlds[name] = customWorld
        database.saveWorld(customWorld)
        Logger.success("Custom world '$name' created successfully!")
        return world
    }

    fun getCustomWorld(worldName: String): CustomWorld? = worlds[worldName]
    fun getAllWorlds(): List<CustomWorld> = worlds.values.toList()
    fun getWorldsByBiome(biomeKey: String): List<CustomWorld> = worlds.values.filter { it.biome.key == biomeKey }
    fun isCustomWorld(worldName: String): Boolean = worlds.containsKey(worldName)

    fun unregisterWorld(worldName: String) {
        worlds.remove(worldName); database.deleteWorld(worldName)
        Logger.info("Unregistered custom world '$worldName'.")
    }

    private fun loadFromDatabase() {
        val records = database.loadAllWorlds()
        var loaded = 0
        for (record in records) {
            val worldName = record["world_name"] as? String ?: continue
            if (Bukkit.getWorld(worldName) != null) {
                worlds[worldName] = record.toCustomWorld() ?: continue
                loaded++
            }
        }
        Logger.info("Loaded $loaded custom world records from database.")
    }
}
