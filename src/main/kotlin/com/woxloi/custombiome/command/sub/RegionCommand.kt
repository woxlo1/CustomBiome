package com.woxloi.custombiome.command.sub

import com.woxloi.custombiome.api.BiomeRegistry
import com.woxloi.custombiome.region.RegionManager
import com.woxloi.custombiome.utils.Msg
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

object RegionCommand {

    fun execute(sender: CommandSender, args: List<String>) {
        if (!sender.hasPermission("custombiome.region")) {
            Msg.send(sender, "&cこのコマンドを実行する権限がありません。")
            return
        }
        when (args.getOrNull(0)?.lowercase()) {
            "set"    -> executeSet(sender, args.drop(1))
            "assign" -> executeAssign(sender, args.drop(1))
            "clear"  -> executeClear(sender, args.drop(1))
            "list"   -> executeList(sender)
            else     -> Msg.send(sender, "&c使い方: /cbiome region <set|assign|clear|list>")
        }
    }

    private fun executeSet(sender: CommandSender, args: List<String>) {
        if (sender !is Player) { Msg.send(sender, "&cプレイヤーのみ実行可能です。"); return }
        val biomeKey = args.getOrNull(0)
        if (biomeKey == null) { Msg.send(sender, "&c使い方: /cbiome region set <biome_key>"); return }
        val biome = BiomeRegistry.get(biomeKey)
        if (biome == null) { Msg.send(sender, "&cバイオーム '&e$biomeKey&c' が見つかりません。"); return }

        if (RegionManager.assignBiomeToSelection(sender, biome)) {
            Msg.send(sender, "&aWE選択範囲に &e${biome.displayName} &aを割り当てました！")
        } else {
            Msg.send(sender, "&cWE選択範囲がありません。先に &e//wand &cで範囲選択してください。")
        }
    }

    private fun executeAssign(sender: CommandSender, args: List<String>) {
        val regionId = args.getOrNull(0)
        val biomeKey = args.getOrNull(1)
        if (regionId == null || biomeKey == null) {
            Msg.send(sender, "&c使い方: /cbiome region assign <region_id> <biome_key>"); return
        }
        val biome = BiomeRegistry.get(biomeKey)
        if (biome == null) { Msg.send(sender, "&cバイオーム '&e$biomeKey&c' が見つかりません。"); return }
        val worldName  = if (sender is Player) sender.world.name else "world"
        val assignedBy = if (sender is Player) sender.uniqueId.toString() else "console"

        if (RegionManager.assignBiomeToRegion(regionId, worldName, biome, assignedBy)) {
            Msg.send(sender, "&aリージョン '&e$regionId&a' に &e${biome.displayName} &aを割り当てました！")
        } else {
            Msg.send(sender, "&cリージョン '&e$regionId&c' が見つかりません。WG で作成してください。")
        }
    }

    private fun executeClear(sender: CommandSender, args: List<String>) {
        val regionId = args.getOrNull(0)
        if (regionId == null) { Msg.send(sender, "&c使い方: /cbiome region clear <region_id>"); return }
        if (RegionManager.removeRegion(regionId)) {
            Msg.send(sender, "&aリージョン '&e$regionId&a' の割り当てを解除しました。")
        } else {
            Msg.send(sender, "&cリージョン '&e$regionId&c' の割り当てが見つかりません。")
        }
    }

    private fun executeList(sender: CommandSender) {
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

    fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        return when (args.size) {
            1 -> listOf("set", "assign", "clear", "list").filter { it.startsWith(args[0].lowercase()) }
            2 -> when (args[0].lowercase()) {
                "set", "assign" -> BiomeRegistry.keys().filter { it.startsWith(args[1]) }
                "clear"         -> RegionManager.getAllRegions().map { it.regionId }.filter { it.startsWith(args[1]) }
                else            -> emptyList()
            }
            3 -> if (args[0].lowercase() == "assign")
                BiomeRegistry.keys().filter { it.startsWith(args[2]) }
            else emptyList()
            else -> emptyList()
        }
    }
}
