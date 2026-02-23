package com.woxloi.custombiome.generator

import com.woxloi.custombiome.biome.*
import com.woxloi.devapi.utils.Logger
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.TreeType as BukkitTreeType
import org.bukkit.block.Block
import java.util.Random

/**
 * チャンク生成後に地物（木・植生・鉱石）を配置する。
 * Bukkit の World#generateTree / Block#setType を使って同期処理する。
 */
object FeaturePlacer {

    fun populate(chunk: Chunk, biome: CustomBiome, heightMap: Array<IntArray>, seed: Long) {
        val rng    = Random(seed xor ((chunk.x * 341873128712L) + (chunk.z * 132897987541L)))
        val world  = chunk.world
        val feat   = biome.features
        val blocks = biome.blocks

        // ---- 木 ----
        if (feat.trees.enabled && rng.nextDouble() < feat.trees.chance) {
            val count = rng.nextInt(feat.trees.maxPerChunk.coerceAtLeast(1)) + 1
            val type  = pickWeighted(feat.trees.types, rng)?.let { toBukkitTreeType(it.type) }
                ?: BukkitTreeType.TREE
            repeat(count) {
                val bx = rng.nextInt(14) + 1
                val bz = rng.nextInt(14) + 1
                val by = heightMap[bx][bz]
                val loc = org.bukkit.Location(world,
                    (chunk.x * 16 + bx).toDouble(),
                    by.toDouble(),
                    (chunk.z * 16 + bz).toDouble()
                )
                runCatching { world.generateTree(loc, type) }
            }
        }

        // ---- 植生 ----
        if (feat.vegetation.enabled) {
            for (plant in feat.vegetation.plants) {
                val count = rng.nextInt(plant.maxPerChunk.coerceAtLeast(1)) + 1
                repeat(count) {
                    if (rng.nextDouble() < plant.chance) {
                        val bx = rng.nextInt(16)
                        val bz = rng.nextInt(16)
                        val by = heightMap[bx][bz]
                        val block = world.getBlockAt(
                            chunk.x * 16 + bx,
                            by,
                            chunk.z * 16 + bz
                        )
                        placePlant(block, plant.block)
                    }
                }
            }
        }

        // ---- 鉱石 ----
        if (feat.ores.enabled) {
            for (vein in feat.ores.veins) {
                if (rng.nextDouble() >= vein.chance) continue
                val originX = chunk.x * 16 + rng.nextInt(16)
                val originY = rng.nextInt((vein.maxHeight - vein.minHeight).coerceAtLeast(1)) + vein.minHeight
                val originZ = chunk.z * 16 + rng.nextInt(16)
                placeOreVein(world, originX, originY, originZ, vein, rng)
            }
        }

        // ---- 表面装飾 ----
        for (deco in blocks.surfaceDecorations) {
            if (rng.nextDouble() < deco.chance) {
                val bx = rng.nextInt(16)
                val bz = rng.nextInt(16)
                val by = heightMap[bx][bz]
                val block = world.getBlockAt(chunk.x * 16 + bx, by, chunk.z * 16 + bz)
                if (block.type == blocks.surface) {
                    block.type = deco.block
                }
            }
        }
    }

    // ----------------------------------------------------------------
    // 内部ヘルパー
    // ----------------------------------------------------------------

    private fun placePlant(ground: Block, material: Material) {
        val above = ground.getRelative(0, 1, 0)
        if (above.type == Material.AIR && ground.type.isSolid) {
            above.type = material
        }
    }

    private fun placeOreVein(
        world: org.bukkit.World,
        ox: Int, oy: Int, oz: Int,
        vein: OreVein,
        rng: Random
    ) {
        repeat(vein.veinSize) {
            val dx = rng.nextInt(5) - 2
            val dy = rng.nextInt(3) - 1
            val dz = rng.nextInt(5) - 2
            val block = world.getBlockAt(ox + dx, (oy + dy).coerceIn(vein.minHeight, vein.maxHeight), oz + dz)
            if (block.type == Material.STONE || block.type == Material.DEEPSLATE) {
                block.type = vein.block
            }
        }
    }

    /**
     * 重み付きランダム選択。
     * 型パラメータを削除し、TreeEntry を直接受け取ることで型推論エラーを解消。
     */
    private fun pickWeighted(list: List<TreeEntry>, rng: Random): TreeEntry? {
        if (list.isEmpty()) return null
        val total = list.sumOf { it.weight }
        var roll  = rng.nextInt(total)
        for (entry in list) {
            roll -= entry.weight
            if (roll < 0) return entry
        }
        return list.last()
    }

    private fun toBukkitTreeType(type: TreeType): BukkitTreeType = when (type) {
        TreeType.OAK      -> BukkitTreeType.TREE
        TreeType.BIRCH    -> BukkitTreeType.BIRCH
        TreeType.SPRUCE   -> BukkitTreeType.REDWOOD
        TreeType.JUNGLE   -> BukkitTreeType.JUNGLE
        TreeType.DARK_OAK -> BukkitTreeType.DARK_OAK
        TreeType.ACACIA   -> BukkitTreeType.ACACIA
        TreeType.CHERRY   -> BukkitTreeType.CHERRY
        TreeType.AZALEA   -> BukkitTreeType.AZALEA
    }
}