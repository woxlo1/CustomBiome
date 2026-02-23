package com.woxloi.custombiome.command.sub

import com.woxloi.custombiome.api.BiomeRegistry
import com.woxloi.custombiome.region.RegionManager
import com.woxloi.custombiome.utils.Msg
import com.woxloi.devapi.command.CommandNode
import org.bukkit.entity.Player

object RegionCommand {
    fun node(): CommandNode {
        val root = CommandNode(
            name        = "region",
            aliases     = listOf("r"),
            permission  = "custombiome.region",
            description = "WorldGuard リージョンにバイオームを割り当てます",
            usage       = "/cbiome region <set|assign|clear|list>"
        )

        // /cbiome region set <biome_key>
        root.addChild(CommandNode(
            name        = "set",
            permission  = "custombiome.region",
            description = "選択中の WorldEdit 範囲にバイオームを割り当てます",
            usage       = "/cbiome region set <biome_key>",
            action      = { sender, args ->
                if (sender !is Player) {
                    Msg.send(sender, "&cプレイヤーのみ実行可能です。")
                    return@CommandNode
                }
                val biomeKey = args.getOrNull(0)
                if (biomeKey == null) {
                    Msg.send(sender, "&c使い方: /cbiome region set <biome_key>")
                    return@CommandNode
                }
                val biome = BiomeRegistry.get(biomeKey)
                if (biome == null) {
                    Msg.send(sender, "&cバイオーム '&e$biomeKey&c' が見つかりません。")
                    return@CommandNode
                }

                if (RegionManager.assignBiomeToSelection(sender, biome)) {
                    Msg.send(sender, "&aWE選択範囲に &e${biome.displayName} &aを割り当てました！")
                } else {
                    Msg.send(sender, "&cWE選択範囲がありません。先に &e//wand &cで範囲選択してください。")
                }
            },
            tabProvider = { _, args ->
                if (args.size <= 1) BiomeRegistry.keys().toList() else emptyList()
            }
        ))

        // /cbiome region assign <region_id> <biome_key>
        root.addChild(CommandNode(
            name        = "assign",
            permission  = "custombiome.region",
            description = "既存の WG リージョンにバイオームを割り当てます",
            usage       = "/cbiome region assign <region_id> <biome_key>",
            action      = { sender, args ->
                val regionId = args.getOrNull(0)
                val biomeKey = args.getOrNull(1)
                if (regionId == null || biomeKey == null) {
                    Msg.send(sender, "&c使い方: /cbiome region assign <region_id> <biome_key>")
                    return@CommandNode
                }
                val biome = BiomeRegistry.get(biomeKey)
                if (biome == null) {
                    Msg.send(sender, "&cバイオーム '&e$biomeKey&c' が見つかりません。")
                    return@CommandNode
                }
                val worldName  = if (sender is Player) sender.world.name else "world"
                val assignedBy = if (sender is Player) sender.uniqueId.toString() else "console"

                if (RegionManager.assignBiomeToRegion(regionId, worldName, biome, assignedBy)) {
                    Msg.send(sender, "&aリージョン '&e$regionId&a' に &e${biome.displayName} &aを割り当てました！")
                } else {
                    Msg.send(sender, "&cリージョン '&e$regionId&c' が見つかりません。WG で作成してください。")
                }
            },
            tabProvider = { _, args ->
                when (args.size) {
                    1    -> RegionManager.getAllRegions().map { it.regionId }
                    2    -> BiomeRegistry.keys().toList()
                    else -> emptyList()
                }
            }
        ))

        // /cbiome region clear <region_id>
        root.addChild(CommandNode(
            name        = "clear",
            permission  = "custombiome.region",
            description = "リージョンのバイオーム割り当てを解除します",
            usage       = "/cbiome region clear <region_id>",
            action      = { sender, args ->
                val regionId = args.getOrNull(0)
                if (regionId == null) {
                    Msg.send(sender, "&c使い方: /cbiome region clear <region_id>")
                    return@CommandNode
                }
                if (RegionManager.removeRegion(regionId)) {
                    Msg.send(sender, "&aリージョン '&e$regionId&a' の割り当てを解除しました。")
                } else {
                    Msg.send(sender, "&cリージョン '&e$regionId&c' の割り当てが見つかりません。")
                }
            },
            tabProvider = { _, args ->
                if (args.size <= 1) RegionManager.getAllRegions().map { it.regionId } else emptyList()
            }
        ))

        // /cbiome region list
        root.addChild(CommandNode(
            name        = "list",
            permission  = "custombiome.region",
            description = "割り当て済みリージョン一覧を表示します",
            usage       = "/cbiome region list",
            action      = { sender, _ ->
                val regions = RegionManager.getAllRegions()
                if (regions.isEmpty()) {
                    Msg.send(sender, "&c割り当て済みリージョンがありません。")
                } else {
                    Msg.send(sender, "&e割り当て済みリージョン &7(${regions.size} 件):")
                    regions.forEach { r ->
                        Msg.sendRaw(sender, "  &a${r.regionId} &7[${r.worldName}] &7→ ${r.biome.displayName}")
                    }
                }
            }
        ))

        return root
    }
}
