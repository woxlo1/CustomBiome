package com.woxloi.custombiome.ui

import com.woxloi.custombiome.biome.CustomBiome
import com.woxloi.custombiome.world.WorldManager
import com.woxloi.devapi.menu.MenuBuilder
import com.woxloi.devapi.menu.MenuItem
import com.woxloi.devapi.menu.MenuManager
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * バイオームの詳細情報を表示する GUI（3行）。
 * 戻るボタン・TP ボタン・ワールド一覧ボタンを配置。
 */
object BiomeMenuDetail {

    fun open(player: Player, biome: CustomBiome) {
        val menu = MenuBuilder("§8§l[ §a${biome.displayName} §8§l]")
            .size(3)
            .build()

        // --- 中央: バイオームアイコン ---
        menu.setItem(13, MenuItem(
            material    = biome.icon,
            displayName = biome.displayName,
            lore        = listOf(
                "§7${biome.description}",
                "",
                "§e地形  §f${biome.terrain.type.name}",
                "§e高さ  §f${biome.terrain.minHeight}～${biome.terrain.maxHeight}",
                "§e温度  §f${biome.weather.temperature}",
                "§e湿度  §f${biome.weather.humidity}",
                "§e雨確率 §f${(biome.weather.rainChance * 100).toInt()}%",
                "",
                "§e木タイプ §f${biome.features.trees.types.joinToString { it.type.name }}",
                "§e鉱石   §f${biome.features.ores.veins.size} 種類"
            )
        )
        )

        // --- 左: 戻るボタン ---
        menu.setItem(9, MenuItem(
            material    = Material.ARROW,
            displayName = "§7← 一覧に戻る",
            onClick     = { p, _ -> BiomeMenuMain.open(p) }
        ))

        // --- 右: ワールド生成へ ---
        menu.setItem(17, MenuItem(
            material    = Material.GRASS_BLOCK,
            displayName = "§aワールドを生成",
            lore        = listOf("§7このバイオームのワールドを新規生成します", "§c権限が必要です"),
            onClick     = { p, _ ->
                if (player.hasPermission("custombiome.generate")) {
                    player.closeInventory()
                    player.sendMessage("§eワールド名を入力してください（またはEnterでスキップ）")
                    // チャット入力待ちは別途 ChatInputListener で実装可能
                    // 簡易: コマンドを提示
                    player.performCommand("cbiome generate ${biome.key}")
                } else {
                    player.sendMessage("§c権限がありません: custombiome.generate")
                }
            }
        ))

        // --- TP ボタン（既存ワールドがある場合のみ有効化）---
        val existingWorlds = WorldManager.getWorldsByBiome(biome.key)
        if (existingWorlds.isNotEmpty()) {
            menu.setItem(11, MenuItem(
                material    = Material.ENDER_PEARL,
                displayName = "§bテレポート",
                lore        = existingWorlds.take(5).map { "§7• ${it.worldName}" } +
                              if (existingWorlds.size > 5) listOf("§7…他 ${existingWorlds.size - 5} 件") else emptyList(),
                onClick     = { p, _ ->
                    if (player.hasPermission("custombiome.tp")) {
                        val target = existingWorlds.first()
                        val world  = org.bukkit.Bukkit.getWorld(target.worldName)
                        if (world != null) {
                            player.closeInventory()
                            player.teleport(world.spawnLocation)
                            player.sendMessage("§a${target.worldName} §eにテレポートしました")
                        } else {
                            player.sendMessage("§cワールドがロードされていません: ${target.worldName}")
                        }
                    } else {
                        player.sendMessage("§c権限がありません: custombiome.tp")
                    }
                }
            ))
        }

        // --- 枠（ガラス）---
        val glass = MenuItem(Material.GRAY_STAINED_GLASS_PANE, " ")
        for (i in 0 until 27) {
            if (menu.getInventory().getItem(i) == null) {
                menu.setItem(i, glass)
            }
        }

        MenuManager.open(player, menu)
    }
}
