package com.woxloi.custombiome.utils

import org.bukkit.Location
import org.bukkit.World
import kotlin.math.sqrt

object BiomeHelper {
    fun horizontalDistance(a: Location, b: Location): Double {
        val dx = a.x - b.x; val dz = a.z - b.z
        return sqrt(dx * dx + dz * dz)
    }
    fun getTopSolidY(world: World, x: Int, z: Int): Int {
        for (y in world.maxHeight downTo world.minHeight) {
            if (world.getBlockAt(x, y, z).type.isSolid) return y
        }
        return world.minHeight
    }
    fun chunkToWorld(chunkCoord: Int): Int = chunkCoord * 16
    fun worldToChunk(worldCoord: Int): Int = worldCoord shr 4
    fun normalizeNoise(value: Double): Double = (value + 1.0) / 2.0
    fun lerp(a: Double, b: Double, t: Double): Double = a + (b - a) * t.coerceIn(0.0, 1.0)
    fun color(s: String): String = s.replace("&", "§")
    fun prefix(message: String, prefix: String = "&7[&aCB&7] "): String = color(prefix + message)
}
