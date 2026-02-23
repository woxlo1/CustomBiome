package com.woxloi.custombiome.biome

import org.bukkit.Material

/**
 * 隣接する2つのバイオーム間でブロックをブレンドするユーティリティ。
 * 境界付近でノイズ値を使って自然な移行を生成する。
 */
object BiomeBlender {

    /**
     * 2つのバイオームのブロックパレットを weight でブレンドして
     * 使用するブロックを返す。
     *
     * @param biomeA  第1バイオーム
     * @param biomeB  第2バイオーム
     * @param weight  biomeA の比率（0.0 = 完全biomeB, 1.0 = 完全biomeA）
     * @param random  乱数（0.0～1.0）
     * @param layer   ブロック層（"surface" / "subsurface" / "deep"）
     */
    fun blendBlock(
        biomeA: CustomBiome,
        biomeB: CustomBiome,
        weight: Double,
        random: Double,
        layer: String
    ): Material {
        val blockA = getBlock(biomeA.blocks, layer)
        val blockB = getBlock(biomeB.blocks, layer)

        // weight を確率として使い、どちらのブロックを使うか決定
        return if (random < weight) blockA else blockB
    }

    /**
     * 境界付近の自然な移行度（0.0～1.0）を計算する。
     * distanceToBorder: バイオーム境界からの距離（チャンク単位で呼び出し元が計算）
     * blendRadius: ブレンドが始まる境界からの距離（ブロック数）
     */
    fun calculateWeight(distanceToBorder: Double, blendRadius: Double = 8.0): Double {
        if (distanceToBorder >= blendRadius) return 1.0
        if (distanceToBorder <= 0.0) return 0.0
        // スムーステップ補間
        val t = distanceToBorder / blendRadius
        return t * t * (3.0 - 2.0 * t)
    }

    /**
     * バイオームの高さをブレンドして補間した高さを返す。
     */
    fun blendHeight(heightA: Int, heightB: Int, weight: Double): Int {
        return (heightA * weight + heightB * (1.0 - weight)).toInt()
    }

    // ----------------------------------------------------------------
    // 内部ヘルパー
    // ----------------------------------------------------------------

    private fun getBlock(palette: BlockPaletteSettings, layer: String): Material =
        when (layer) {
            "surface"    -> palette.surface
            "subsurface" -> palette.subsurface
            "deep"       -> palette.deep
            else         -> palette.deep
        }
}
