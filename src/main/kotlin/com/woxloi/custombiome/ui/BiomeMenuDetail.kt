package com.woxloi.custombiome.ui

import com.woxloi.custombiome.biome.CustomBiome
import com.woxloi.custombiome.world.WorldManager
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * バイオームの詳細情報を表示する GUI（3行）。
 * WoxloiDevAPI の MenuBuilder 等を使わず Bukkit Inventory API で実装。
 */
object BiomeMenuDetail {

    fun open(player: Player, biome: CustomBiome) {
        val menu = GuiMenu("§8§l[ §a${biome.displayName} §8§l]", 27)

        // 中央: バイオームアイコン
        menu.setItem(13, ItemBuilder.build(
            material    = biome.icon,
            displayName = biome.displayName,
            lore        = listOf(
                "&7${biome.description}",
                "",
                "&e地形  &f${biome.terrain.type.name}",
                "&e高さ  &f${biome.terrain.minHeight}～${biome.terrain.maxHeight}",
                "&e温度  &f${biome.weather.temperature}",
                "&e湿度  &f${biome.weather.humidity}",
                "&e雨確率 &f${(biome.weather.rainChance * 100).toInt()}%",
                "",
                "&e木タイプ &f${biome.features.trees.types.joinToString { it.type.name }}",
                "&e鉱石   &f${biome.features.ores.veins.size} 種類"
            )
        ))

        // 左: 戻るボタン
        menu.setItem(9, ItemBuilder.build(Material.ARROW, "&7← 一覧に戻る")) { p ->
            BiomeMenuMain.open(p)
        }

        // 右: ワールド生成
        menu.setItem(17, ItemBuilder.build(
            Material.GRASS_BLOCK,
            "&aワールドを生成",
            listOf("&7このバイオームのワールドを新規生成します", "&c権限が必要です")
        )) { p ->
            if (p.hasPermission("custombiome.generate")) {
                p.closeInventory()
                p.performCommand("cbiome generate ${biome.key}")
            } else {
                p.sendMessage("§c権限がありません: custombiome.generate")
            }
        }

        // TP ボタン（既存ワールドがある場合のみ）
        val existingWorlds = WorldManager.getWorldsByBiome(biome.key)
        if (existingWorlds.isNotEmpty()) {
            val lore = existingWorlds.take(5).map { "&7• ${it.worldName}" } +
                    if (existingWorlds.size > 5) listOf("&7…他 ${existingWorlds.size - 5} 件") else emptyList()
            menu.setItem(11, ItemBuilder.build(Material.ENDER_PEARL, "&bテレポート", lore)) { p ->
                if (p.hasPermission("custombiome.tp")) {
                    val target = existingWorlds.first()
                    val world  = Bukkit.getWorld(target.worldName)
                    if (world != null) {
                        p.closeInventory()
                        p.teleport(world.spawnLocation)
                        p.sendMessage("§a${target.worldName} §eにテレポートしました")
                    } else {
                        p.sendMessage("§cワールドがロードされていません: ${target.worldName}")
                    }
                } else {
                    p.sendMessage("§c権限がありません: custombiome.tp")
                }
            }
        }

        // 枠（ガラス）
        val glass = ItemBuilder.build(Material.GRAY_STAINED_GLASS_PANE, " ")
        for (i in 0 until 27) {
            if (menu.inventory.getItem(i) == null) menu.setItem(i, glass)
        }

        player.openInventory(menu.inventory)
    }
}
