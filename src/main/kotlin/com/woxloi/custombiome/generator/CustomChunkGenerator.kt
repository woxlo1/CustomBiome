package com.woxloi.custombiome.generator

import com.woxloi.custombiome.biome.CustomBiome
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import java.util.Random

class CustomChunkGenerator(
    private val biome: CustomBiome,
    private val seed: Long,
    private val noiseScale: Double = 0.005,
    private val baseHeight: Int = 64,
    private val heightMultiplier: Int = 64,
    private val octaves: Int = 6,
    private val persistence: Double = 0.5,
    private val lacunarity: Double = 2.0
) : ChunkGenerator() {

    private val terrain = TerrainGenerator(seed, noiseScale, baseHeight, heightMultiplier, octaves, persistence, lacunarity)

    override fun generateChunkData(world: World, random: Random, chunkX: Int, chunkZ: Int, biomeGrid: BiomeGrid): ChunkData {
        val data      = createChunkData(world)
        val palette   = biome.blocks
        val heightMap = terrain.getChunkHeightMap(chunkX, chunkZ, biome)
        val minY      = world.minHeight

        for (x in 0..15) {
            for (z in 0..15) {
                val surfaceY = heightMap[x][z]
                data.setBlock(x, minY, z, Material.BEDROCK)
                data.setBlock(x, minY + 1, z, Material.BEDROCK)
                for (y in (minY + 2) until (surfaceY - 4)) data.setBlock(x, y, z, palette.deep)
                for (y in (surfaceY - 4).coerceAtLeast(minY + 2) until surfaceY) data.setBlock(x, y, z, palette.subsurface)
                if (surfaceY > palette.seaLevel) {
                    data.setBlock(x, surfaceY, z, palette.surface)
                } else {
                    data.setBlock(x, surfaceY, z, palette.subsurface)
                    for (y in (surfaceY + 1)..palette.seaLevel) data.setBlock(x, y, z, palette.fluid)
                }
                if (biome.terrain.type.name == "OCEAN") {
                    for (y in (surfaceY + 1)..palette.seaLevel) {
                        if (data.getType(x, y, z) == Material.AIR) data.setBlock(x, y, z, palette.fluid)
                    }
                }
            }
        }
        return data
    }

    override fun shouldGenerateDecorations(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int) = false
    override fun shouldGenerateMobs(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int) = true
    override fun shouldGenerateCaves(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int) = biome.features.structures.caves
    override fun getDefaultPopulators(world: World): MutableList<org.bukkit.generator.BlockPopulator> = mutableListOf()

    fun getHeightAt(x: Int, z: Int): Int = terrain.getHeight(x, z, biome)
}
