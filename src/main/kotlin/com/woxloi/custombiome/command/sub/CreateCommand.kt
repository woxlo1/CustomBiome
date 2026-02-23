package com.woxloi.custombiome.command.sub

import com.woxloi.custombiome.CustomBiomePlugin
import com.woxloi.custombiome.utils.Msg
import com.woxloi.devapi.command.CommandNode
import java.io.File

object CreateCommand {
    fun node() = CommandNode(
        name        = "create",
        aliases     = listOf("new"),
        permission  = "custombiome.create",
        description = "バイオーム定義YAMLのテンプレートを生成します",
        usage       = "/cbiome create <biome_name>",
        action      = { sender, args ->
            val name = args.getOrNull(0)
            if (name == null) {
                Msg.send(sender, "&c使い方: /cbiome create <biome_name>")
                return@CommandNode
            }

            val key      = name.lowercase().replace(" ", "_")
            val biomesDir = File(CustomBiomePlugin.instance.dataFolder, "biomes")
            if (!biomesDir.exists()) biomesDir.mkdirs()

            val file = File(biomesDir, "$key.yml")
            if (file.exists()) {
                Msg.send(sender, "&c'&e$key.yml&c' はすでに存在します。")
                return@CommandNode
            }

            file.writeText(generateTemplate(key, name))
            Msg.send(sender, "&aテンプレートを生成しました: &ebiomes/$key.yml")
            Msg.send(sender, "&7編集後に &e/cbiome reload &7でロードできます。")
        }
    )

    private fun generateTemplate(key: String, displayName: String): String = """
# ===========================
# カスタムバイオーム定義: $key
# ===========================
name: "$key"
display-name: "&a$displayName"
description: "ここに説明を入力してください"
icon: GRASS_BLOCK

terrain:
  type: HILLS
  min-height: 60
  max-height: 120
  noise-scale-multiplier: 1.0
  height-multiplier: 1.0

blocks:
  surface: GRASS_BLOCK
  subsurface: DIRT
  deep: STONE
  bedrock-layer: DEEPSLATE
  fluid: WATER
  sea-level: 62
  surface-decorations: []

features:
  trees:
    enabled: true
    types:
      - type: OAK
        weight: 100
    max-per-chunk: 4
    chance: 0.5
  vegetation:
    enabled: true
    plants:
      - block: GRASS
        chance: 0.5
        max-per-chunk: 32
  ores:
    enabled: true
    veins:
      - block: COAL_ORE
        min-height: 0
        max-height: 128
        vein-size: 17
        chance: 0.8
  structures:
    caves: true
    dungeons: false

spawns:
  day:
    - entity: COW
      min: 1
      max: 4
      weight: 10
  night:
    - entity: ZOMBIE
      min: 2
      max: 4
      weight: 8
  water: []

weather:
  temperature: 0.5
  humidity: 0.5
  rain-chance: 0.3
  sky-color: "7BA4FF"

ambience:
  particles:
    enabled: false
    type: ENCHANTMENT_TABLE
    density: 0.01
  sounds: []
""".trimIndent()
}
