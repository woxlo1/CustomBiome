package com.woxloi.custombiome.ui

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

/**
 * ItemStack を簡単に作るビルダーユーティリティ。
 */
object ItemBuilder {

    fun build(
        material: Material,
        displayName: String,
        lore: List<String> = emptyList()
    ): ItemStack {
        val item = ItemStack(material)
        val meta: ItemMeta = item.itemMeta ?: return item
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName))
        if (lore.isNotEmpty()) {
            meta.lore = lore.map { ChatColor.translateAlternateColorCodes('&', it) }
        }
        item.itemMeta = meta
        return item
    }
}
