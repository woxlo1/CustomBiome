package com.woxloi.custombiome.ui

import com.woxloi.custombiome.api.BiomeRegistry
import com.woxloi.custombiome.biome.CustomBiome
import com.woxloi.devapi.menu.Menu
import com.woxloi.devapi.menu.MenuBuilder
import com.woxloi.devapi.menu.MenuItem
import com.woxloi.devapi.menu.MenuManager
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * /cbiome list で開くバイオーム一覧 GUI。
 * WoxloiDevAPI の MenuBuilder / MenuPage を使ってページング対応。
 */
object BiomeMenuMain {

    fun open(player: Player) {
        val biomes = BiomeRegistry.getAll().sortedBy { it.key }

        val items = biomes.map { biome ->
            MenuItem(
                material    = biome.icon,
                displayName = biome.displayName,
                lore        = buildLore(biome),
                onClick     = { p, _ -> BiomeMenuDetail.open(p, biome) }
            )
        }

        // バイオームが 0 件の場合はプレースホルダを表示
        val displayItems = items.ifEmpty {
            listOf(
                MenuItem(Material.BARRIER, "§cバイオームが登録されていません",
                    listOf("§7biomes/ フォルダにYAMLを配置してください"))
            )
        }

        val menu = MenuBuilder("§8§l[ §a§lCustomBiome §8§l]")
            .size(6)
            .withPaging(45)
            .apply { displayItems.forEach { addItem(it) } }
            .build()

        MenuManager.open(player, menu)
    }

    private fun buildLore(biome: CustomBiome): List<String> = listOf(
        "§7${biome.description}",
        "§8—————————————",
        "§e地形タイプ §f${biome.terrain.type.name}",
        "§e高さ範囲  §f${biome.terrain.minHeight} ～ ${biome.terrain.maxHeight}",
        "§e温度      §f${biome.weather.temperature}",
        "§e湿度      §f${biome.weather.humidity}",
        "§8—————————————",
        "§aクリックで詳細を見る"
    )
}
