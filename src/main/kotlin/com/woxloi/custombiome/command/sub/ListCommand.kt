package com.woxloi.custombiome.command.sub

import com.woxloi.custombiome.api.BiomeRegistry
import com.woxloi.custombiome.ui.BiomeMenuMain
import com.woxloi.custombiome.utils.Msg
import com.woxloi.devapi.command.CommandNode
import org.bukkit.entity.Player

object ListCommand {
    fun node() = CommandNode(
        name        = "list",
        aliases     = listOf("ls", "l"),
        description = "登録済みバイオームを一覧表示します",
        usage       = "/cbiome list",
        action      = { sender, _ ->
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
    )
}
