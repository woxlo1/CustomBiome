package com.woxloi.custombiome.ui

import com.woxloi.custombiome.api.BiomeRegistry
import com.woxloi.custombiome.biome.CustomBiome
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * /cbiome list で開くバイオーム一覧 GUI。
 * WoxloiDevAPI の MenuBuilder 等を使わず Bukkit Inventory API で実装。
 * 最大 45 件をページなしで表示（それ以上は省略メッセージ）。
 */
object BiomeMenuMain {

    private const val PAGE_SIZE = 45

    fun open(player: Player) {
        val biomes = BiomeRegistry.getAll().sortedBy { it.key }
        val menu = GuiMenu("§8§l[ §a§lCustomBiome §8§l]", 54)

        // バイオームアイテムを配置（0～44スロット）
        biomes.take(PAGE_SIZE).forEachIndexed { index, biome ->
            val item = ItemBuilder.build(
                material    = biome.icon,
                displayName = biome.displayName,
                lore        = buildLore(biome)
            )
            menu.setItem(index, item) { p -> BiomeMenuDetail.open(p, biome) }
        }

        // バイオームが 0 件
        if (biomes.isEmpty()) {
            val barrier = ItemBuilder.build(
                Material.BARRIER,
                "&cバイオームが登録されていません",
                listOf("&7biomes/ フォルダにYAMLを配置してください")
            )
            menu.setItem(22, barrier)
        }

        // 45件超の場合は最終スロットに案内
        if (biomes.size > PAGE_SIZE) {
            val more = ItemBuilder.build(
                Material.PAPER,
                "&7…他 ${biomes.size - PAGE_SIZE} 件",
                listOf("&7/cbiome list でさらに確認できます")
            )
            menu.setItem(53, more)
        }

        // 枠（ガラス）で残りスロットを埋める
        val glass = ItemBuilder.build(Material.GRAY_STAINED_GLASS_PANE, " ")
        for (i in 45..53) {
            if (menu.inventory.getItem(i) == null) menu.setItem(i, glass)
        }

        player.openInventory(menu.inventory)
    }

    private fun buildLore(biome: CustomBiome): List<String> = listOf(
        "&7${biome.description}",
        "&8—————————————",
        "&e地形タイプ &f${biome.terrain.type.name}",
        "&e高さ範囲  &f${biome.terrain.minHeight} ～ ${biome.terrain.maxHeight}",
        "&e温度      &f${biome.weather.temperature}",
        "&e湿度      &f${biome.weather.humidity}",
        "&8—————————————",
        "&aクリックで詳細を見る"
    )
}
