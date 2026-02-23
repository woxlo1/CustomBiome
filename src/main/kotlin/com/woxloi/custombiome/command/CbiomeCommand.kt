package com.woxloi.custombiome.command

import com.woxloi.custombiome.api.BiomeRegistry
import com.woxloi.custombiome.command.sub.*
import com.woxloi.custombiome.utils.Msg
import com.woxloi.devapi.command.CommandNode

/**
 * /cbiome ルートコマンドを構築して返す。
 * WoxloiDevAPI の CommandNode ツリーを使用。
 */
object CbiomeCommand {

    fun createRoot(): CommandNode {
        val root = CommandNode(
            name        = "cbiome",
            aliases     = listOf("cb", "custombiome"),
            description = "CustomBiome メインコマンド",
            usage       = "/cbiome <subcommand>",
            action      = { sender, _ ->
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
        )

        root.addChild(ListCommand.node())
        root.addChild(InfoCommand.node())
        root.addChild(CreateCommand.node())
        root.addChild(GenerateCommand.node())
        root.addChild(TeleportCommand.node())
        root.addChild(RegionCommand.node())
        root.addChild(ReloadCommand.node())

        return root
    }
}
