package com.woxloi.custombiome.command.sub

import com.woxloi.custombiome.utils.Msg
import com.woxloi.custombiome.utils.WandItemUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object WandCommand {

    fun execute(sender: CommandSender, args: List<String>) {
        if (sender !is Player) {
            Msg.send(sender, "&cプレイヤーのみ実行できます。")
            return
        }
        if (!sender.hasPermission("custombiome.region")) {
            Msg.send(sender, "&cこのコマンドを実行する権限がありません。")
            return
        }

        sender.inventory.addItem(WandItemUtil.createWand())
        Msg.send(sender, "&bワンドを付与しました！ &7左クリックで Pos1、右クリックで Pos2 を指定してください。")
    }
}
