package com.woxloi.custombiome.generator

import com.woxloi.custombiome.biome.*
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.TreeType as BukkitTreeType
import org.bukkit.block.Block
import java.util.Random

object FeaturePlacer {

    fun populate(chunk: Chunk, biome: CustomBiome, heightMap: Array<IntArray>, seed: Long) {
        val rng    = Random(seed xor ((chunk.x * 341873128712L) + (chunk.z * 132897987541L)))
        val world  = chunk.world
        val feat   = biome.features
        val blocks = biome.blocks

        // 木の生成は generateTree 内部でも隣接更新が走ることがあるため runCatching で保護
        if (feat.trees.enabled && rng.nextDouble() < feat.trees.chance) {
            val count = rng.nextInt(feat.trees.maxPerChunk.coerceAtLeast(1)) + 1
            val type  = pickWeighted(feat.trees.types, rng)?.let { toBukkitTreeType(it.type) } ?: BukkitTreeType.TREE
            repeat(count) {
                val bx  = rng.nextInt(14) + 1; val bz = rng.nextInt(14) + 1
                val by  = heightMap[bx][bz]
                val loc = org.bukkit.Location(world,
                    (chunk.x * 16 + bx).toDouble(), by.toDouble(), (chunk.z * 16 + bz).toDouble())
                runCatching { world.generateTree(loc, type) }
            }
        }

        if (feat.vegetation.enabled) {
            for (plant in feat.vegetation.plants) {
                val count = rng.nextInt(plant.maxPerChunk.coerceAtLeast(1)) + 1
                repeat(count) {
                    if (rng.nextDouble() < plant.chance) {
                        val bx    = rng.nextInt(16); val bz = rng.nextInt(16)
                        val block = world.getBlockAt(chunk.x * 16 + bx, heightMap[bx][bz], chunk.z * 16 + bz)
                        placePlant(block, plant.block)
                    }
                }
            }
        }

        if (feat.ores.enabled) {
            for (vein in feat.ores.veins) {
                if (rng.nextDouble() >= vein.chance) continue
                val ox = chunk.x * 16 + rng.nextInt(16)
                val oy = rng.nextInt((vein.maxHeight - vein.minHeight).coerceAtLeast(1)) + vein.minHeight
                val oz = chunk.z * 16 + rng.nextInt(16)
                placeOreVein(world, ox, oy, oz, vein, rng)
            }
        }

        for (deco in blocks.surfaceDecorations) {
            if (rng.nextDouble() < deco.chance) {
                val bx    = rng.nextInt(16); val bz = rng.nextInt(16)
                val block = world.getBlockAt(chunk.x * 16 + bx, heightMap[bx][bz], chunk.z * 16 + bz)
                // applyPhysics=false で隣接チャンクへの更新伝播を抑制
                if (block.type == blocks.surface) block.setType(deco.block, false)
            }
        }
    }

    private fun placePlant(ground: Block, material: Material) {
        val above = ground.getRelative(0, 1, 0)
        if (above.type == Material.AIR && ground.type.isSolid) {
            // applyPhysics=false: ブロック更新を隣接チャンクに伝播させない
            // これが今回のタイムアウトの直接原因だった箇所
            above.setType(material, false)
        }
    }

    private fun placeOreVein(world: org.bukkit.World, ox: Int, oy: Int, oz: Int, vein: OreVein, rng: Random) {
        repeat(vein.veinSize) {
            val dx    = rng.nextInt(5) - 2; val dy = rng.nextInt(3) - 1; val dz = rng.nextInt(5) - 2
            val block = world.getBlockAt(ox + dx, (oy + dy).coerceIn(vein.minHeight, vein.maxHeight), oz + dz)
            // 鉱石も同様に applyPhysics=false
            if (block.type == Material.STONE || block.type == Material.DEEPSLATE)
                block.setType(vein.block, false)
        }
    }

    private fun pickWeighted(list: List<TreeEntry>, rng: Random): TreeEntry? {
        if (list.isEmpty()) return null
        val total = list.sumOf { it.weight }
        var roll  = rng.nextInt(total)
        for (entry in list) { roll -= entry.weight; if (roll < 0) return entry }
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
