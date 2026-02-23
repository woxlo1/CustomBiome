package com.woxloi.custombiome.command.sub

import com.woxloi.custombiome.api.BiomeRegistry
import com.woxloi.custombiome.utils.Msg
import com.woxloi.custombiome.world.WorldManager
import com.woxloi.devapi.command.CommandNode
import com.woxloi.devapi.utils.scheduler.TaskScheduler
import org.bukkit.entity.Player

object GenerateCommand {
    fun node() = CommandNode(
        name        = "generate",
        aliases     = listOf("gen", "g"),
        permission  = "custombiome.generate",
        description = "カスタムバイオームのワールドを生成します",
        usage       = "/cbiome generate <biome_key> [world_name] [seed]",
        action      = { sender, args ->
            val biomeKey = args.getOrNull(0)
            if (biomeKey == null) {
                Msg.send(sender, "&c使い方: /cbiome generate <biome_key> [world_name] [seed]")
                return@CommandNode
            }

            val biome = BiomeRegistry.get(biomeKey)
            if (biome == null) {
                Msg.send(sender, "&cバイオーム '&e$biomeKey&c' が見つかりません。")
                return@CommandNode
            }

            val worldName = args.getOrNull(1)
            val seed      = args.getOrNull(2)?.toLongOrNull() ?: 0L
            val createdBy = if (sender is Player) sender.uniqueId.toString() else "console"

            Msg.send(sender, "&eワールドを生成中… しばらくお待ちください。")

            TaskScheduler.runSync {
                val world = WorldManager.createWorld(biome, worldName, seed, createdBy)
                if (world != null) {
                    Msg.send(sender, "&aワールド '&e${world.name}&a' を生成しました！")
                    if (sender is Player) {
                        sender.teleport(world.spawnLocation)
                        Msg.send(sender, "&bテレポートしました。")
                    }
                } else {
                    Msg.send(sender, "&cワールドの生成に失敗しました。コンソールを確認してください。")
                }
            }
        },
        tabProvider = { _, args ->
            when (args.size) {
                1    -> BiomeRegistry.keys().toList()
                else -> emptyList()
            }
        }
    )
}
