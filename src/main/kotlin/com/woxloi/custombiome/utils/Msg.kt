package com.woxloi.custombiome.utils

import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

object Msg {
    private var prefix: String = "§e[§b§lCustomBiome§e] §r"

    fun init(configPrefix: String) { prefix = color(configPrefix) }

    fun send(sender: CommandSender, message: String) = sender.sendMessage(prefix + color(message))
    fun sendRaw(sender: CommandSender, message: String) = sender.sendMessage(color(message))
    fun color(text: String): String = ChatColor.translateAlternateColorCodes('&', text)
}
