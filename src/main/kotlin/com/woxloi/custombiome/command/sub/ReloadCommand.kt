package com.woxloi.custombiome.command.sub

import com.woxloi.custombiome.CustomBiomePlugin
import com.woxloi.custombiome.api.BiomeRegistry
import com.woxloi.custombiome.utils.Msg
import com.woxloi.devapi.command.CommandNode

object ReloadCommand {
    fun node() = CommandNode(
        name        = "reload",
        aliases     = listOf("rl"),
        permission  = "custombiome.reload",
        description = "設定・バイオーム定義をリロードします",
        usage       = "/cbiome reload",
        action      = { sender, _ ->
            Msg.send(sender, "&eリロード中…")
            runCatching {
                CustomBiomePlugin.instance.reloadPlugin()
                Msg.send(sender, "&aリロード完了！ バイオーム: &e${BiomeRegistry.count()} &a件")
            }.onFailure { e ->
                Msg.send(sender, "&cリロードに失敗しました: ${e.message}")
                e.printStackTrace()
            }
        }
    )
}
