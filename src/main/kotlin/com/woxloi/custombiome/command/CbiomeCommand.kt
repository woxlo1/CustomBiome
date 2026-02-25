package com.woxloi.custombiome.command

import com.woxloi.custombiome.api.BiomeRegistry
import com.woxloi.custombiome.command.sub.*
import com.woxloi.custombiome.utils.Msg
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

/**
 * /cbiome ルートコマンド。
 * Bukkit 標準の CommandExecutor / TabCompleter を実装。
 * WoxloiDevAPI の CommandNode / CommandRegistry を使わない。
 */
class CbiomeCommand : CommandExecutor, TabCompleter {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): Boolean {
        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }
        when (args[0].lowercase()) {
            "list", "ls", "l"        -> ListCommand.execute(sender, args.drop(1))
            "info", "i"              -> InfoCommand.execute(sender, args.drop(1))
            "create", "new"          -> CreateCommand.execute(sender, args.drop(1))
            "generate", "gen", "g"   -> GenerateCommand.execute(sender, args.drop(1))
            "tp", "teleport", "goto" -> TeleportCommand.execute(sender, args.drop(1))
            "region", "r"            -> RegionCommand.execute(sender, args.drop(1))
            "reload", "rl"           -> ReloadCommand.execute(sender, args.drop(1))
            else -> Msg.send(sender, "&cサブコマンドが見つかりません。 /cbiome でヘルプを確認してください。")
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String> {
        if (args.size == 1) {
            val subs = listOf("list", "info", "create", "generate", "tp", "region", "reload")
            return subs.filter { it.startsWith(args[0].lowercase()) }
        }
        return when (args[0].lowercase()) {
            "info", "i"              -> InfoCommand.tabComplete(sender, args.drop(1))
            "generate", "gen", "g"   -> GenerateCommand.tabComplete(sender, args.drop(1))
            "tp", "teleport", "goto" -> TeleportCommand.tabComplete(sender, args.drop(1))
            "region", "r"            -> RegionCommand.tabComplete(sender, args.drop(1))
            else                     -> emptyList()
        }
    }

    private fun sendHelp(sender: CommandSender) {
        Msg.send(sender, "&e=== CustomBiome コマンド一覧 ===")
        Msg.sendRaw(sender, "  &a/cbiome list &7- バイオーム一覧")
        Msg.sendRaw(sender, "  &a/cbiome info <key> &7- バイオーム詳細")
        Msg.sendRaw(sender, "  &a/cbiome create <n> &7- テンプレート生成")
        Msg.sendRaw(sender, "  &a/cbiome generate <key> [name] [seed] &7- ワールド生成")
        Msg.sendRaw(sender, "  &a/cbiome tp <world> &7- テレポート")
        Msg.sendRaw(sender, "  &a/cbiome region set/assign/clear/list &7- WGリージョン管理")
        Msg.sendRaw(sender, "  &a/cbiome reload &7- 設定リロード")
        Msg.send(sender, "&7登録バイオーム数: &e${BiomeRegistry.count()}")
    }
}
