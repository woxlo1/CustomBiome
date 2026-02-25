package com.woxloi.custombiome.command.sub

import com.woxloi.custombiome.utils.Msg
import com.woxloi.custombiome.world.WorldManager
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object TeleportCommand {

    fun execute(sender: CommandSender, args: List<String>) {
        if (sender !is Player) {
            Msg.send(sender, "&cプレイヤーのみ実行できます。")
            return
        }
        if (!sender.hasPermission("custombiome.tp")) {
            Msg.send(sender, "&cこのコマンドを実行する権限がありません。")
            return
        }
        val worldName = args.getOrNull(0)
        if (worldName == null) {
            val worlds = WorldManager.getAllWorlds()
            if (worlds.isEmpty()) {
                Msg.send(sender, "&cカスタムワールドがありません。先に &e/cbiome generate &cで生成してください。")
            } else {
                Msg.send(sender, "&eカスタムワールド一覧:")
                worlds.forEach { w -> Msg.sendRaw(sender, "  &a${w.worldName} &7(${w.biome.displayName}&7)") }
                Msg.send(sender, "&7/cbiome tp <world_name> でテレポートできます。")
            }
            return
        }
        val customWorld = WorldManager.getCustomWorld(worldName)
        if (customWorld == null) {
            Msg.send(sender, "&cカスタムワールド '&e$worldName&c' が見つかりません。")
            return
        }
        val world = Bukkit.getWorld(worldName)
        if (world == null) {
            Msg.send(sender, "&cワールド '&e$worldName&c' がロードされていません。")
            return
        }
        sender.teleport(world.spawnLocation)
        Msg.send(sender, "&a${world.name} &7(${customWorld.biome.displayName}&7) &aにテレポートしました！")
    }

    fun tabComplete(sender: CommandSender, args: List<String>): List<String> =
        if (args.size <= 1)
            WorldManager.getAllWorlds().map { it.worldName }.filter { it.startsWith(args.getOrElse(0) { "" }) }
        else emptyList()
}
