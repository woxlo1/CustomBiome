package com.woxloi.custombiome.biome

import org.bukkit.Material

object BiomeBlender {

    fun blendBlock(biomeA: CustomBiome, biomeB: CustomBiome, weight: Double, random: Double, layer: String): Material {
        val blockA = getBlock(biomeA.blocks, layer)
        val blockB = getBlock(biomeB.blocks, layer)
        return if (random < weight) blockA else blockB
    }

    fun calculateWeight(distanceToBorder: Double, blendRadius: Double = 8.0): Double {
        if (distanceToBorder >= blendRadius) return 1.0
        if (distanceToBorder <= 0.0) return 0.0
        val t = distanceToBorder / blendRadius
        return t * t * (3.0 - 2.0 * t)
    }

    fun blendHeight(heightA: Int, heightB: Int, weight: Double): Int =
        (heightA * weight + heightB * (1.0 - weight)).toInt()

    private fun getBlock(palette: BlockPaletteSettings, layer: String): Material = when (layer) {
        "surface"    -> palette.surface
        "subsurface" -> palette.subsurface
        "deep"       -> palette.deep
        else         -> palette.deep
    }
}
