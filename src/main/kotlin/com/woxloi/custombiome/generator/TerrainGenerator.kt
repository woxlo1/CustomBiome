package com.woxloi.custombiome.generator

import com.woxloi.custombiome.biome.CustomBiome
import com.woxloi.custombiome.biome.TerrainType
import com.woxloi.custombiome.utils.BiomeHelper

/**
 * Simplex Noise を使って高さマップを生成する。
 * バイオームの TerrainType と TerrainSettings によってパラメータを切り替える。
 */
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

    /**
     * 指定されたバイオームのブロック座標 (x, z) における地表高さを返す。
     * 戻り値はブロック Y 座標。
     */
    fun getHeight(x: Int, z: Int, biome: CustomBiome): Int {
        val settings = biome.terrain
        val scale    = noiseScale * settings.noiseScaleMultiplier
        val hMul     = heightMultiplier * settings.heightMultiplier

        val raw = noise.octaveNoise2D(
            x = x.toDouble(),
            y = z.toDouble(),
            octaves     = octaves,
            persistence = persistence,
            lacunarity  = lacunarity,
            scale       = scale
        )

        // -1～1 → 0～1 に正規化
        val normalized = (raw + 1.0) / 2.0

        // 地形タイプ別に高さを調整
        val shaped = applyTerrainShape(normalized, settings.type)

        val height = (baseHeight + shaped * hMul).toInt()
        return height.coerceIn(settings.minHeight, settings.maxHeight)
    }

    /**
     * TerrainType によってノイズ値を整形する。
     */
    private fun applyTerrainShape(n: Double, type: TerrainType): Double = when (type) {
        TerrainType.FLAT      -> n * 0.1 + 0.45     // ほぼ平坦
        TerrainType.HILLS     -> n                   // そのまま
        TerrainType.MOUNTAINS -> Math.pow(n, 1.5)    // 高山化（急峻に）
        TerrainType.OCEAN     -> n * 0.3             // 低く抑える
        TerrainType.PLATEAU   -> plateau(n)          // 台地形状
    }

    /** 台地形状：一定値以上は平坦にする */
    private fun plateau(n: Double): Double {
        return if (n > 0.6) 0.6 + (n - 0.6) * 0.1 else n
    }

    /**
     * チャンク全体（16×16）の高さマップを返す。
     * @return Array[16][16] of Y heights
     */
    fun getChunkHeightMap(chunkX: Int, chunkZ: Int, biome: CustomBiome): Array<IntArray> {
        val map = Array(16) { IntArray(16) }
        for (bx in 0..15) {
            for (bz in 0..15) {
                val worldX = chunkX * 16 + bx
                val worldZ = chunkZ * 16 + bz
                map[bx][bz] = getHeight(worldX, worldZ, biome)
            }
        }
        return map
    }
}
