package com.woxloi.custombiome.biome

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.EntityType

// ================================================================
// CustomBiome データクラス群
// バイオームYAMLの構造をそのままKotlinで表現する
// ================================================================

/**
 * カスタムバイオームのルートデータクラス
 */
data class CustomBiome(
    val name: String,
    val displayName: String,
    val description: String,
    val icon: Material,
    val terrain: TerrainSettings,
    val blocks: BlockPaletteSettings,
    val features: FeatureSettings,
    val spawns: SpawnSettings,
    val weather: WeatherSettings,
    val ambience: AmbienceSettings
) {
    /** バイオームの識別キー（小文字・スペース→アンダースコア） */
    val key: String get() = name.lowercase().replace(" ", "_")
}

// ----------------------------------------------------------------
// 地形
// ----------------------------------------------------------------

data class TerrainSettings(
    val type: TerrainType = TerrainType.HILLS,
    val minHeight: Int = 60,
    val maxHeight: Int = 120,
    val noiseScaleMultiplier: Double = 1.0,
    val heightMultiplier: Double = 1.0
)

enum class TerrainType {
    FLAT,
    HILLS,
    MOUNTAINS,
    OCEAN,
    PLATEAU
}

// ----------------------------------------------------------------
// ブロックパレット
// ----------------------------------------------------------------

data class BlockPaletteSettings(
    val surface: Material = Material.GRASS_BLOCK,
    val subsurface: Material = Material.DIRT,
    val deep: Material = Material.STONE,
    val bedrockLayer: Material = Material.DEEPSLATE,
    val fluid: Material = Material.WATER,
    val seaLevel: Int = 62,
    val surfaceDecorations: List<SurfaceDecoration> = emptyList()
)

data class SurfaceDecoration(
    val block: Material,
    val chance: Double   // 0.0 ～ 1.0
)

// ----------------------------------------------------------------
// 地物
// ----------------------------------------------------------------

data class FeatureSettings(
    val trees: TreeSettings = TreeSettings(),
    val vegetation: VegetationSettings = VegetationSettings(),
    val ores: OreSettings = OreSettings(),
    val structures: StructureSettings = StructureSettings()
)

data class TreeSettings(
    val enabled: Boolean = true,
    val types: List<TreeEntry> = listOf(TreeEntry(TreeType.OAK, 100)),
    val maxPerChunk: Int = 4,
    val chance: Double = 0.5
)

data class TreeEntry(
    val type: TreeType,
    val weight: Int
)

enum class TreeType {
    OAK, BIRCH, SPRUCE, JUNGLE, DARK_OAK, ACACIA, CHERRY, AZALEA
}

data class VegetationSettings(
    val enabled: Boolean = true,
    val plants: List<PlantEntry> = emptyList()
)

data class PlantEntry(
    val block: Material,
    val chance: Double,
    val maxPerChunk: Int
)

data class OreSettings(
    val enabled: Boolean = true,
    val veins: List<OreVein> = emptyList()
)

data class OreVein(
    val block: Material,
    val minHeight: Int,
    val maxHeight: Int,
    val veinSize: Int,
    val chance: Double
)

data class StructureSettings(
    val caves: Boolean = true,
    val dungeons: Boolean = false
)

// ----------------------------------------------------------------
// スポーン
// ----------------------------------------------------------------

data class SpawnSettings(
    val day: List<SpawnEntry> = emptyList(),
    val night: List<SpawnEntry> = emptyList(),
    val water: List<SpawnEntry> = emptyList()
)

data class SpawnEntry(
    val entity: EntityType,
    val min: Int,
    val max: Int,
    val weight: Int
)

// ----------------------------------------------------------------
// 天気
// ----------------------------------------------------------------

data class WeatherSettings(
    val temperature: Double = 0.5,
    val humidity: Double = 0.5,
    val rainChance: Double = 0.3,
    val skyColor: String = "7BA4FF"
)

// ----------------------------------------------------------------
// アンビエンス
// ----------------------------------------------------------------

data class AmbienceSettings(
    val particles: ParticleAmbienceSettings = ParticleAmbienceSettings(),
    val sounds: List<SoundEntry> = emptyList()
)

data class ParticleAmbienceSettings(
    val enabled: Boolean = false,
    val type: String = "ENCHANTMENT_TABLE",
    val density: Double = 0.01
)

data class SoundEntry(
    val sound: String,
    val chance: Double
)
