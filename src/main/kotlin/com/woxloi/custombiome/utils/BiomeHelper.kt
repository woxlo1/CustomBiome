package com.woxloi.custombiome.utils

import org.bukkit.Location
import org.bukkit.World
import kotlin.math.sqrt

/**
 * CustomBiome プラグイン全体で使う汎用ユーティリティ。
 */
object BiomeHelper {

    /**
     * 2 つの Location 間の水平距離（Y を無視）を返す。
     */
    fun horizontalDistance(a: Location, b: Location): Double {
        val dx = a.x - b.x
        val dz = a.z - b.z
        return sqrt(dx * dx + dz * dz)
    }

    /**
     * 指定ワールドで最も高い固体ブロックの Y 座標を返す（簡易版）。
     */
    fun getTopSolidY(world: World, x: Int, z: Int): Int {
        for (y in world.maxHeight downTo world.minHeight) {
            val block = world.getBlockAt(x, y, z)
            if (block.type.isSolid) return y
        }
        return world.minHeight
    }

    /**
     * チャンク座標をワールド座標に変換（X/Z の最小角）。
     */
    fun chunkToWorld(chunkCoord: Int): Int = chunkCoord * 16

    /**
     * ワールド座標をチャンク座標に変換。
     */
    fun worldToChunk(worldCoord: Int): Int = worldCoord shr 4

    /**
     * ノイズ値 (-1.0 ～ 1.0) を 0.0 ～ 1.0 に正規化する。
     */
    fun normalizeNoise(value: Double): Double = (value + 1.0) / 2.0

    /**
     * 線形補間。
     */
    fun lerp(a: Double, b: Double, t: Double): Double = a + (b - a) * t.coerceIn(0.0, 1.0)

    /**
     * カラーコードを変換（& → §）。
     */
    fun color(s: String): String = s.replace("&", "§")

    /**
     * プレフィックス付きメッセージを整形する。
     */
    fun prefix(message: String, prefix: String = "&7[&aCB&7] "): String =
        color(prefix + message)
}
