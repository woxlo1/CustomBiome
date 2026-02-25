package com.woxloi.custombiome.utils

import com.woxloi.custombiome.CustomBiomePlugin
import org.bukkit.ChatColor

object Logger {
    private const val PREFIX = "§e[§b§lCustomBiome§e] §r"

    fun info(message: String) =
        CustomBiomePlugin.instance.server.consoleSender.sendMessage(PREFIX + ChatColor.WHITE + message)
    fun success(message: String) =
        CustomBiomePlugin.instance.server.consoleSender.sendMessage(PREFIX + ChatColor.GREEN + message)
    fun warn(message: String) =
        CustomBiomePlugin.instance.server.consoleSender.sendMessage(PREFIX + ChatColor.YELLOW + message)
    fun error(message: String) =
        CustomBiomePlugin.instance.server.consoleSender.sendMessage(PREFIX + ChatColor.RED + message)
}
