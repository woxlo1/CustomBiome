package com.woxloi.custombiome.command.sub

import com.woxloi.custombiome.api.BiomeRegistry
import com.woxloi.custombiome.ui.BiomeMenuMain
import com.woxloi.custombiome.utils.Msg
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object ListCommand {

    fun execute(sender: CommandSender, args: List<String>) {
        if (sender is Player) {
            BiomeMenuMain.open(sender)
        } else {
            val biomes = BiomeRegistry.getAll()
            if (biomes.isEmpty()) {
                Msg.send(sender, "&cバイオームが登録されていません。")
            } else {
                Msg.send(sender, "&e登録済みバイオーム一覧 (${biomes.size} 件):")
                biomes.forEach { b ->
                    Msg.sendRaw(sender, "  &a${b.key} &7- ${b.description}")
                }
            }
        }
    }
}
