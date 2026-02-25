package com.woxloi.custombiome.generator

import com.woxloi.custombiome.biome.CustomBiome
import com.woxloi.custombiome.biome.TerrainType

class TerrainGenerator(
    private val seed: Long,
    private val noiseScale: Double = 0.005,
    private val baseHeight: Int = 64,
    private val heightMultiplier: Int = 64,
    private val octaves: Int = 6,
    private val persistence: Double = 0.5,
    private val lacunarity: Double = 2.0
) {
    private val noise = SimplexNoise(seed)

    fun getHeight(x: Int, z: Int, biome: CustomBiome): Int {
        val settings = biome.terrain
        val scale    = noiseScale * settings.noiseScaleMultiplier
        val hMul     = heightMultiplier * settings.heightMultiplier
        val raw      = noise.octaveNoise2D(x.toDouble(), z.toDouble(), octaves, persistence, lacunarity, scale)
        val normalized = (raw + 1.0) / 2.0
        val shaped     = applyTerrainShape(normalized, settings.type)
        val height     = (baseHeight + shaped * hMul).toInt()
        return height.coerceIn(settings.minHeight, settings.maxHeight)
    }

    private fun applyTerrainShape(n: Double, type: TerrainType): Double = when (type) {
        TerrainType.FLAT      -> n * 0.1 + 0.45
        TerrainType.HILLS     -> n
        TerrainType.MOUNTAINS -> Math.pow(n, 1.5)
        TerrainType.OCEAN     -> n * 0.3
        TerrainType.PLATEAU   -> plateau(n)
    }

    private fun plateau(n: Double): Double =
        if (n > 0.6) 0.6 + (n - 0.6) * 0.1 else n

    fun getChunkHeightMap(chunkX: Int, chunkZ: Int, biome: CustomBiome): Array<IntArray> {
        val map = Array(16) { IntArray(16) }
        for (bx in 0..15) for (bz in 0..15) {
            map[bx][bz] = getHeight(chunkX * 16 + bx, chunkZ * 16 + bz, biome)
        }
        return map
    }
}
