package com.woxloi.custombiome.command.sub

import com.woxloi.custombiome.CustomBiomePlugin
import com.woxloi.custombiome.api.BiomeRegistry
import com.woxloi.custombiome.utils.Msg
import org.bukkit.command.CommandSender

object ReloadCommand {

    fun execute(sender: CommandSender, args: List<String>) {
        if (!sender.hasPermission("custombiome.reload")) {
            Msg.send(sender, "&cこのコマンドを実行する権限がありません。")
            return
        }
        Msg.send(sender, "&eリロード中…")
        runCatching {
            CustomBiomePlugin.instance.reloadPlugin()
            Msg.send(sender, "&aリロード完了！ バイオーム: &e${BiomeRegistry.count()} &a件")
        }.onFailure { e ->
            Msg.send(sender, "&cリロードに失敗しました: ${e.message}")
            e.printStackTrace()
        }
    }
}
