package com.woxloi.custombiome.utils

import com.woxloi.custombiome.biome.BlockPaletteSettings
import org.bukkit.Material

/**
 * ブロックパレットの選択・検証ユーティリティ。
 * FeaturePlacer や ChunkGenerator から使用する。
 */
object BlockPalette {

    /**
     * Y 座標に対応するパレットブロックを返す。
     */
    fun getBlock(palette: BlockPaletteSettings, y: Int, surfaceY: Int): Material {
        return when {
            y == surfaceY                    -> palette.surface
            y >= surfaceY - 4               -> palette.subsurface
            y <= 5                          -> palette.bedrockLayer
            else                            -> palette.deep
        }
    }

    /**
     * 指定 Material がプレースホルダとして配置できるか判定する。
     */
    fun isPlaceable(material: Material): Boolean =
        material.isBlock && !material.name.contains("AIR")

    /**
     * 水中かどうかを判定する。
     */
    fun isFluid(material: Material): Boolean =
        material == Material.WATER || material == Material.LAVA

    /**
     * Surface decoration の候補リストからランダムに 1 つ返す。
     * chance の合計が 1.0 を超えないことを推奨。
     */
    fun pickDecoration(
        decorations: List<com.woxloi.custombiome.biome.SurfaceDecoration>,
        random: java.util.Random
    ): Material? {
        val roll = random.nextDouble()
        var cumulative = 0.0
        for (deco in decorations) {
            cumulative += deco.chance
            if (roll < cumulative) return deco.block
        }
        return null
    }
}
