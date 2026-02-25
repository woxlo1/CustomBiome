package com.woxloi.custombiome.command.sub

import com.woxloi.custombiome.CustomBiomePlugin
import com.woxloi.custombiome.api.BiomeRegistry
import com.woxloi.custombiome.utils.Msg
import com.woxloi.custombiome.world.WorldManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object GenerateCommand {

    fun execute(sender: CommandSender, args: List<String>) {
        if (!sender.hasPermission("custombiome.generate")) {
            Msg.send(sender, "&cこのコマンドを実行する権限がありません。")
            return
        }
        val biomeKey = args.getOrNull(0)
        if (biomeKey == null) {
            Msg.send(sender, "&c使い方: /cbiome generate <biome_key> [world_name] [seed]")
            return
        }
        val biome = BiomeRegistry.get(biomeKey)
        if (biome == null) {
            Msg.send(sender, "&cバイオーム '&e$biomeKey&c' が見つかりません。")
            return
        }
        val worldName = args.getOrNull(1)
        val seed      = args.getOrNull(2)?.toLongOrNull() ?: 0L
        val createdBy = if (sender is Player) sender.uniqueId.toString() else "console"

        Msg.send(sender, "&eワールドを生成中… しばらくお待ちください。")

        // Bukkit スケジューラでメインスレッド実行（WorldCreator はメインスレッド必須）
        CustomBiomePlugin.instance.server.scheduler.runTask(CustomBiomePlugin.instance, Runnable {
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
        })
    }

    fun tabComplete(sender: CommandSender, args: List<String>): List<String> =
        when (args.size) {
            1    -> BiomeRegistry.keys().filter { it.startsWith(args[0]) }
            else -> emptyList()
        }
}
