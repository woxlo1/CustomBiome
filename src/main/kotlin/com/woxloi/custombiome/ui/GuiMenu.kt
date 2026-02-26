package com.woxloi.custombiome.ui

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class GuiMenu(val title: String, val size: Int) : InventoryHolder {

    private val inventory: Inventory = Bukkit.createInventory(this, size, title)
    private val clickHandlers = mutableMapOf<Int, (Player) -> Unit>()

    override fun getInventory(): Inventory = inventory

    fun setItem(slot: Int, item: ItemStack, onClick: ((Player) -> Unit)? = null) {
        inventory.setItem(slot, item)
        if (onClick != null) clickHandlers[slot] = onClick
    }

    fun handleClick(player: Player, slot: Int) {
        clickHandlers[slot]?.invoke(player)
    }
}

class GuiListener : Listener {

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val holder = event.inventory.holder as? GuiMenu ?: return
        event.isCancelled = true
        val player = event.whoClicked as? Player ?: return
        if (event.rawSlot < 0 || event.rawSlot >= event.inventory.size) return
        holder.handleClick(player, event.rawSlot)
    }
}
