package com.woxloi.custombiome.utils

import com.woxloi.custombiome.CustomBiomePlugin
import org.bukkit.ChatColor

object Logger {

    private var prefix: String = "§e[§b§lCustomBiomeLogger§e] §r"

    fun info(message: String) {
        CustomBiomePlugin.instance.server.consoleSender.sendMessage(prefix + ChatColor.WHITE + message)
    }

    fun success(message: String) {
        CustomBiomePlugin.instance.server.consoleSender.sendMessage(prefix + ChatColor.GREEN + message)
    }

    fun warn(message: String) {
        CustomBiomePlugin.instance.server.consoleSender.sendMessage(prefix + ChatColor.YELLOW + message)
    }

    fun error(message: String) {
        CustomBiomePlugin.instance.server.consoleSender.sendMessage(prefix + ChatColor.RED + message)
    }
}