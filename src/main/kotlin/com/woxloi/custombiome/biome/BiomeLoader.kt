package com.woxloi.custombiome.biome

import com.woxloi.custombiome.utils.Logger
import org.bukkit.Material
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.EntityType
import java.io.File

/**
 * バイオーム定義YAML → CustomBiome データクラスへの変換を担当。
 */
object BiomeLoader {

    fun load(file: File): CustomBiome? {
        return try {
            val cfg = YamlConfiguration.loadConfiguration(file)
            parseCustomBiome(cfg, file.nameWithoutExtension)
        } catch (e: Exception) {
            Logger.error("Failed to load biome file '${file.name}': ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // ----------------------------------------------------------------
    // 内部パース処理
    // ----------------------------------------------------------------

    private fun parseCustomBiome(cfg: YamlConfiguration, fallbackName: String): CustomBiome {
        val name        = cfg.getString("name", fallbackName)!!
        val displayName = color(cfg.getString("display-name", name)!!)
        val description = cfg.getString("description", "")!!
        val icon        = parseMaterial(cfg.getString("icon"), Material.GRASS_BLOCK)

        return CustomBiome(
            name        = name,
            displayName = displayName,
            description = description,
            icon        = icon,
            terrain     = parseTerrain(cfg),
            blocks      = parseBlocks(cfg),
            features    = parseFeatures(cfg),
            spawns      = parseSpawns(cfg),
            weather     = parseWeather(cfg),
            ambience    = parseAmbience(cfg)
        )
    }

    private fun parseTerrain(cfg: YamlConfiguration): TerrainSettings {
        val s = cfg.getConfigurationSection("terrain")
        return TerrainSettings(
            type                 = parseEnum(s?.getString("type"), TerrainType.HILLS),
            minHeight            = s?.getInt("min-height", 60) ?: 60,
            maxHeight            = s?.getInt("max-height", 120) ?: 120,
            noiseScaleMultiplier = s?.getDouble("noise-scale-multiplier", 1.0) ?: 1.0,
            heightMultiplier     = s?.getDouble("height-multiplier", 1.0) ?: 1.0
        )
    }

    private fun parseBlocks(cfg: YamlConfiguration): BlockPaletteSettings {
        val s = cfg.getConfigurationSection("blocks")
        val deco = s?.getMapList("surface-decorations")?.mapNotNull { map ->
            val block  = parseMaterialOrNull(map["block"]?.toString()) ?: return@mapNotNull null
            val chance = (map["chance"] as? Number)?.toDouble() ?: 0.0
            SurfaceDecoration(block, chance)
        } ?: emptyList()

        return BlockPaletteSettings(
            surface            = parseMaterial(s?.getString("surface"), Material.GRASS_BLOCK),
            subsurface         = parseMaterial(s?.getString("subsurface"), Material.DIRT),
            deep               = parseMaterial(s?.getString("deep"), Material.STONE),
            bedrockLayer       = parseMaterial(s?.getString("bedrock-layer"), Material.DEEPSLATE),
            fluid              = parseMaterial(s?.getString("fluid"), Material.WATER),
            seaLevel           = s?.getInt("sea-level", 62) ?: 62,
            surfaceDecorations = deco
        )
    }

    private fun parseFeatures(cfg: YamlConfiguration): FeatureSettings {
        val fs = cfg.getConfigurationSection("features")

        // --- 木 ---
        val ts = fs?.getConfigurationSection("trees")
        val treeTypes = ts?.getMapList("types")?.mapNotNull { map ->
            val typeName = map["type"]?.toString() ?: return@mapNotNull null
            val weight   = (map["weight"] as? Number)?.toInt() ?: 1
            val treeType = parseEnumOrNull<TreeType>(typeName) ?: return@mapNotNull null
            TreeEntry(treeType, weight)
        } ?: emptyList()
        val trees = TreeSettings(
            enabled     = ts?.getBoolean("enabled", true) ?: true,
            types       = treeTypes.ifEmpty { listOf(TreeEntry(TreeType.OAK, 100)) },
            maxPerChunk = ts?.getInt("max-per-chunk", 4) ?: 4,
            chance      = ts?.getDouble("chance", 0.5) ?: 0.5
        )

        // --- 植生 ---
        val vs = fs?.getConfigurationSection("vegetation")
        val plants = vs?.getMapList("plants")?.mapNotNull { map ->
            val block  = parseMaterialOrNull(map["block"]?.toString()) ?: return@mapNotNull null
            val chance = (map["chance"] as? Number)?.toDouble() ?: 0.0
            val max    = (map["max-per-chunk"] as? Number)?.toInt() ?: 16
            PlantEntry(block, chance, max)
        } ?: emptyList()
        val vegetation = VegetationSettings(
            enabled = vs?.getBoolean("enabled", true) ?: true,
            plants  = plants
        )

        // --- 鉱石 ---
        val os = fs?.getConfigurationSection("ores")
        val veins = os?.getMapList("veins")?.mapNotNull { map ->
            val block    = parseMaterialOrNull(map["block"]?.toString()) ?: return@mapNotNull null
            val minH     = (map["min-height"] as? Number)?.toInt() ?: 0
            val maxH     = (map["max-height"] as? Number)?.toInt() ?: 128
            val veinSize = (map["vein-size"] as? Number)?.toInt() ?: 8
            val chance   = (map["chance"] as? Number)?.toDouble() ?: 0.5
            OreVein(block, minH, maxH, veinSize, chance)
        } ?: emptyList()
        val ores = OreSettings(
            enabled = os?.getBoolean("enabled", true) ?: true,
            veins   = veins
        )

        // --- 構造物 ---
        val ss = fs?.getConfigurationSection("structures")
        val structures = StructureSettings(
            caves    = ss?.getBoolean("caves", true) ?: true,
            dungeons = ss?.getBoolean("dungeons", false) ?: false
        )

        return FeatureSettings(trees, vegetation, ores, structures)
    }

    private fun parseSpawns(cfg: YamlConfiguration): SpawnSettings {
        fun parseList(key: String): List<SpawnEntry> {
            return cfg.getMapList("spawns.$key").mapNotNull { map ->
                val entityName = map["entity"]?.toString() ?: return@mapNotNull null
                val entityType = runCatching { EntityType.valueOf(entityName) }.getOrNull()
                    ?: return@mapNotNull null
                val min    = (map["min"] as? Number)?.toInt() ?: 1
                val max    = (map["max"] as? Number)?.toInt() ?: 3
                val weight = (map["weight"] as? Number)?.toInt() ?: 5
                SpawnEntry(entityType, min, max, weight)
            }
        }
        return SpawnSettings(
            day   = parseList("day"),
            night = parseList("night"),
            water = parseList("water")
        )
    }

    private fun parseWeather(cfg: YamlConfiguration): WeatherSettings {
        val s = cfg.getConfigurationSection("weather")
        return WeatherSettings(
            temperature = s?.getDouble("temperature", 0.5) ?: 0.5,
            humidity    = s?.getDouble("humidity", 0.5) ?: 0.5,
            rainChance  = s?.getDouble("rain-chance", 0.3) ?: 0.3,
            skyColor    = s?.getString("sky-color", "7BA4FF") ?: "7BA4FF"
        )
    }

    private fun parseAmbience(cfg: YamlConfiguration): AmbienceSettings {
        val s  = cfg.getConfigurationSection("ambience")
        val ps = s?.getConfigurationSection("particles")
        val particles = ParticleAmbienceSettings(
            enabled = ps?.getBoolean("enabled", false) ?: false,
            type    = ps?.getString("type", "ENCHANTMENT_TABLE") ?: "ENCHANTMENT_TABLE",
            density = ps?.getDouble("density", 0.01) ?: 0.01
        )
        val sounds = s?.getMapList("sounds")?.mapNotNull { map ->
            val sound  = map["sound"]?.toString() ?: return@mapNotNull null
            val chance = (map["chance"] as? Number)?.toDouble() ?: 0.01
            SoundEntry(sound, chance)
        } ?: emptyList()
        return AmbienceSettings(particles, sounds)
    }

    // ----------------------------------------------------------------
    // ヘルパー（JVMシグネチャが衝突しないよう関数名を分ける）
    // ----------------------------------------------------------------

    private fun color(s: String) = s.replace("&", "§")

    /** null や不正な名前の場合は default（non-null）を返す */
    private fun parseMaterial(name: String?, default: Material): Material =
        name?.let { runCatching { Material.valueOf(it.uppercase()) }.getOrNull() } ?: default

    /** null や不正な名前の場合は null を返す */
    private fun parseMaterialOrNull(name: String?): Material? =
        name?.let { runCatching { Material.valueOf(it.uppercase()) }.getOrNull() }

    /** null や不正な名前の場合は default（non-null）を返す */
    private inline fun <reified T : Enum<T>> parseEnum(name: String?, default: T): T =
        name?.let { runCatching { enumValueOf<T>(it.uppercase()) }.getOrNull() } ?: default

    /** null や不正な名前の場合は null を返す */
    private inline fun <reified T : Enum<T>> parseEnumOrNull(name: String?): T? =
        name?.let { runCatching { enumValueOf<T>(it.uppercase()) }.getOrNull() }
}