package com.woxloi.custombiome.command.sub

import com.woxloi.custombiome.utils.Msg
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

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

        val wand = ItemStack(Material.WOODEN_AXE).apply {
            val meta = itemMeta!!
            meta.setDisplayName("§b§lCustomBiome §eワンド")
            meta.lore = listOf(
                "§7左クリック §f→ Pos1 を設定",
                "§7右クリック §f→ Pos2 を設定",
                "§7範囲設定後に §e/cbiome region set <key>"
            )
            meta.addEnchant(Enchantment.DURABILITY, 1, true)
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
            itemMeta = meta
        }

        sender.inventory.addItem(wand)
        Msg.send(sender, "&bワンドを付与しました！ &7左クリックで Pos1、右クリックで Pos2 を指定してください。")
    }
}
