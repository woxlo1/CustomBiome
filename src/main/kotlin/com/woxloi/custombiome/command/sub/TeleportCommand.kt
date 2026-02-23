package com.woxloi.custombiome.command.sub

import com.woxloi.custombiome.utils.Msg
import com.woxloi.custombiome.world.WorldManager
import com.woxloi.devapi.command.CommandNode
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object TeleportCommand {
    fun node() = CommandNode(
        name        = "tp",
        aliases     = listOf("teleport", "goto"),
        permission  = "custombiome.tp",
        description = "カスタムバイオームワールドへテレポートします",
        usage       = "/cbiome tp <world_name>",
        action      = { sender, args ->
            if (sender !is Player) {
                Msg.send(sender, "&cプレイヤーのみ実行できます。")
                return@CommandNode
            }

            val worldName = args.getOrNull(0)
            if (worldName == null) {
                val worlds = WorldManager.getAllWorlds()
                if (worlds.isEmpty()) {
                    Msg.send(sender, "&cカスタムワールドがありません。先に &e/cbiome generate &cで生成してください。")
                } else {
                    Msg.send(sender, "&eカスタムワールド一覧:")
                    worlds.forEach { w ->
                        Msg.sendRaw(sender, "  &a${w.worldName} &7(${w.biome.displayName}&7)")
                    }
                    Msg.send(sender, "&7/cbiome tp <world_name> でテレポートできます。")
                }
                return@CommandNode
            }

            val customWorld = WorldManager.getCustomWorld(worldName)
            if (customWorld == null) {
                Msg.send(sender, "&cカスタムワールド '&e$worldName&c' が見つかりません。")
                return@CommandNode
            }

            val world = Bukkit.getWorld(worldName)
            if (world == null) {
                Msg.send(sender, "&cワールド '&e$worldName&c' がロードされていません。")
                return@CommandNode
            }

            sender.teleport(world.spawnLocation)
            Msg.send(sender, "&a${world.name} &7(${customWorld.biome.displayName}&7) &aにテレポートしました！")
        },
        tabProvider = { _, args ->
            if (args.size <= 1) WorldManager.getAllWorlds().map { it.worldName } else emptyList()
        }
    )
}
