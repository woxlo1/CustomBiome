package com.woxloi.custombiome.utils

import com.woxloi.custombiome.biome.BlockPaletteSettings
import com.woxloi.custombiome.biome.SurfaceDecoration
import org.bukkit.Material

object BlockPalette {
    fun getBlock(palette: BlockPaletteSettings, y: Int, surfaceY: Int): Material = when {
        y == surfaceY     -> palette.surface
        y >= surfaceY - 4 -> palette.subsurface
        y <= 5            -> palette.bedrockLayer
        else              -> palette.deep
    }
    fun isPlaceable(material: Material): Boolean = material.isBlock && !material.name.contains("AIR")
    fun isFluid(material: Material): Boolean = material == Material.WATER || material == Material.LAVA
    fun pickDecoration(decorations: List<SurfaceDecoration>, random: java.util.Random): Material? {
        val roll = random.nextDouble(); var cumulative = 0.0
        for (deco in decorations) { cumulative += deco.chance; if (roll < cumulative) return deco.block }
        return null
    }
}
