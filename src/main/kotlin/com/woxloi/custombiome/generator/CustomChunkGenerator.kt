package com.woxloi.custombiome.generator

import com.woxloi.custombiome.biome.CustomBiome
import com.woxloi.custombiome.biome.BlockPaletteSettings
import com.woxloi.custombiome.CustomBiomePlugin
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import java.util.Random

/**
 * Bukkit ChunkGenerator の実装。
 * バイオーム定義をもとにブロックを積み上げてチャンクを生成する。
 *
 * 使い方:
 *   world.generator に CustomChunkGenerator を設定するか、
 *   WorldCreator.generator(CustomChunkGenerator(biome, seed)) を渡す。
 */
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

    private val terrain = TerrainGenerator(
        seed             = seed,
        noiseScale       = noiseScale,
        baseHeight       = baseHeight,
        heightMultiplier = heightMultiplier,
        octaves          = octaves,
        persistence      = persistence,
        lacunarity       = lacunarity
    )

    override fun generateChunkData(
        world: World,
        random: Random,
        chunkX: Int,
        chunkZ: Int,
        biomeGrid: BiomeGrid
    ): ChunkData {
        val data      = createChunkData(world)
        val palette   = biome.blocks
        val heightMap = terrain.getChunkHeightMap(chunkX, chunkZ, biome)
        val minY      = world.minHeight

        for (x in 0..15) {
            for (z in 0..15) {
                val surfaceY = heightMap[x][z]

                // 岩盤層（最下部 2 ブロック）
                data.setBlock(x, minY,     z, Material.BEDROCK)
                data.setBlock(x, minY + 1, z, Material.BEDROCK)

                // 深層
                for (y in (minY + 2) until (surfaceY - 4)) {
                    data.setBlock(x, y, z, palette.deep)
                }

                // 表層直下（3 ブロック）
                for (y in (surfaceY - 4).coerceAtLeast(minY + 2) until surfaceY) {
                    data.setBlock(x, y, z, palette.subsurface)
                }

                // 表層
                if (surfaceY > palette.seaLevel) {
                    data.setBlock(x, surfaceY, z, palette.surface)
                } else {
                    // 水中：表層はサブサーフェス、海水で埋める
                    data.setBlock(x, surfaceY, z, palette.subsurface)
                    for (y in (surfaceY + 1)..palette.seaLevel) {
                        data.setBlock(x, y, z, palette.fluid)
                    }
                }

                // 海面以下の穴を水で埋める（海洋・深海）
                if (biome.terrain.type.name == "OCEAN") {
                    for (y in (surfaceY + 1)..palette.seaLevel) {
                        if (data.getType(x, y, z) == Material.AIR) {
                            data.setBlock(x, y, z, palette.fluid)
                        }
                    }
                }
            }
        }

        return data
    }

    /**
     * 地物配置（木・鉱石）は ChunkPopulate ではなく
     * WorldListener 経由で非同期後に FeaturePlacer.populate を呼ぶ。
     * Bukkit 1.17+ では shouldGenerateDecorations を false にして
     * カスタム装飾のみを使う選択肢もある。
     */
    override fun shouldGenerateDecorations(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int
    ): Boolean = false  // 装飾は WorldListener 経由で手動配置

    override fun shouldGenerateMobs(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int
    ): Boolean = true

    override fun shouldGenerateCaves(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int
    ): Boolean = biome.features.structures.caves

    override fun getDefaultPopulators(world: World): MutableList<org.bukkit.generator.BlockPopulator> =
        mutableListOf()

    /** 高さマップを公開（WorldListener から参照するために） */
    fun getHeightAt(x: Int, z: Int): Int = terrain.getHeight(x, z, biome)
}
