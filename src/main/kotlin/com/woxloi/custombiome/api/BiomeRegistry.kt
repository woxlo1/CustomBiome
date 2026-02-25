package com.woxloi.custombiome.api

import com.woxloi.custombiome.biome.CustomBiome
import com.woxloi.custombiome.biome.BiomeLoader
import com.woxloi.custombiome.utils.Logger
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object BiomeRegistry {
    private val biomes = ConcurrentHashMap<String, CustomBiome>()

    fun register(biome: CustomBiome) {
        biomes[biome.key] = biome
        Logger.info("Registered biome: ${biome.key} (${biome.displayName})")
    }
    fun unregister(key: String) { biomes.remove(key) }
    fun clear() { val c = biomes.size; biomes.clear(); Logger.info("Cleared $c biome(s) from registry.") }
    fun get(key: String): CustomBiome? = biomes[key]
    fun getAll(): List<CustomBiome> = biomes.values.toList()
    fun keys(): Set<String> = biomes.keys.toSet()
    fun contains(key: String): Boolean = biomes.containsKey(key)
    fun count(): Int = biomes.size

    fun loadFromDirectory(dir: File): Int {
        if (!dir.exists()) { Logger.warn("Biome directory not found: ${dir.absolutePath}"); return 0 }
        val files = dir.listFiles { f -> f.extension == "yml" } ?: return 0
        var count = 0
        for (file in files) {
            val biome = BiomeLoader.load(file)
            if (biome != null) { register(biome); count++ }
            else Logger.warn("Skipped invalid biome file: ${file.name}")
        }
        Logger.success("Loaded $count biome(s) from '${dir.name}/'."); return count
    }
}
