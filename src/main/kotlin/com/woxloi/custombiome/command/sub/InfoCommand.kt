package com.woxloi.custombiome.command.sub

import com.woxloi.custombiome.api.BiomeRegistry
import com.woxloi.custombiome.ui.BiomeMenuDetail
import com.woxloi.custombiome.utils.Msg
import com.woxloi.devapi.command.CommandNode
import org.bukkit.entity.Player

object InfoCommand {
    fun node() = CommandNode(
        name        = "info",
        aliases     = listOf("i"),
        description = "指定バイオームの詳細を表示します",
        usage       = "/cbiome info <biome_key>",
        action      = { sender, args ->
            val key = args.getOrNull(0)
            if (key == null) {
                Msg.send(sender, "&c使い方: /cbiome info <biome_key>")
                return@CommandNode
            }
            val biome = BiomeRegistry.get(key)
            if (biome == null) {
                Msg.send(sender, "&cバイオーム '&e$key&c' が見つかりません。")
                return@CommandNode
            }

            if (sender is Player) {
                BiomeMenuDetail.open(sender, biome)
            } else {
                Msg.send(sender, "&e=== ${biome.displayName} &e===")
                Msg.sendRaw(sender, "  &7説明: &f${biome.description}")
                Msg.sendRaw(sender, "  &7地形タイプ: &f${biome.terrain.type.name}")
                Msg.sendRaw(sender, "  &7高さ: &f${biome.terrain.minHeight} ～ ${biome.terrain.maxHeight}")
                Msg.sendRaw(sender, "  &7温度: &f${biome.weather.temperature} &7/ 湿度: &f${biome.weather.humidity}")
                Msg.sendRaw(sender, "  &7鉱石: &f${biome.features.ores.veins.size} 種類 &7/ 木: &f${biome.features.trees.types.size} 種類")
            }
        },
        tabProvider = { _, args ->
            if (args.size <= 1) BiomeRegistry.keys().toList() else emptyList()
        }
    )
}
