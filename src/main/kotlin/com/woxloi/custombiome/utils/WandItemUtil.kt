package com.woxloi.custombiome.utils

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

/**
 * CustomBiome ワンドの生成・識別を一元管理するユーティリティ。
 *
 * 判定には PersistentDataContainer (PDC) を使う。
 * 表示名だけでの判定は rename コマンドで偽装できるため PDC を正とし、
 * 表示名は補助的なチェックにとどめる。
 */
object WandItemUtil {

    // プラグインが初期化される前に NamespacedKey を作れないため遅延初期化
    private val key: NamespacedKey by lazy {
        NamespacedKey(JavaPlugin.getProvidingPlugin(WandItemUtil::class.java), "custombiome_wand")
    }

    private const val WAND_VALUE = "1"

    /** ワンドアイテムを生成して返す */
    fun createWand(): ItemStack {
        val item = ItemStack(Material.WOODEN_AXE)
        val meta = item.itemMeta!!
        meta.setDisplayName("§b§lCustomBiome §eワンド")
        meta.lore = listOf(
            "§7左クリック §f→ Pos1 を設定",
            "§7右クリック §f→ Pos2 を設定",
            "§7範囲設定後に §e/cbiome region set <key>"
        )
        meta.addEnchant(Enchantment.DURABILITY, 1, true)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        // PDC タグを埋め込む — これが本物のワンドの識別子
        meta.persistentDataContainer.set(key, PersistentDataType.STRING, WAND_VALUE)
        item.itemMeta = meta
        return item
    }

    /** アイテムが CustomBiome ワンドかどうかを PDC で判定する */
    fun isWand(item: ItemStack?): Boolean {
        if (item == null || item.type != Material.WOODEN_AXE) return false
        val meta = item.itemMeta ?: return false
        return meta.persistentDataContainer.get(key, PersistentDataType.STRING) == WAND_VALUE
    }
}