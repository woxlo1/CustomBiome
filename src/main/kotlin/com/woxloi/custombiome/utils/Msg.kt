package com.woxloi.custombiome.utils

import org.bukkit.command.CommandSender
import org.bukkit.ChatColor

/**
 * CustomBiome 専用メッセージ送信ユーティリティ。
 * WoxloiDevAPI の Message クラスは使わず、
 * config.yml の settings.prefix を適用して送信する。
 *
 * prefix は CustomBiomePlugin.onEnable() で init() を呼んで初期化する。
 */
object Msg {

    private var prefix: String = "§e[§b§lCustomBiome§e] §r"

    /**
     * CustomBiomePlugin から config の prefix を読み込んで初期化する。
     */
    fun init(configPrefix: String) {
        prefix = color(configPrefix)
    }

    /**
     * prefix 付きでメッセージを送信する。
     */
    fun send(sender: CommandSender, message: String) {
        sender.sendMessage(prefix + color(message))
    }

    /**
     * prefix なしでメッセージを送信する（サブ行など）。
     */
    fun sendRaw(sender: CommandSender, message: String) {
        sender.sendMessage(color(message))
    }

    /**
     * &カラーコード → §カラーコードに変換する。
     */
    fun color(text: String): String =
        ChatColor.translateAlternateColorCodes('&', text)
}
