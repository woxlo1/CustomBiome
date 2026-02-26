package com.woxloi.custombiome

import com.woxloi.custombiome.api.BiomeRegistry
import com.woxloi.custombiome.command.CbiomeCommand
import com.woxloi.custombiome.database.BiomeDatabase
import com.woxloi.custombiome.database.MySQLProvider
import com.woxloi.custombiome.listener.PlayerListener
import com.woxloi.custombiome.listener.WandListener
import com.woxloi.custombiome.listener.WorldListener
import com.woxloi.custombiome.region.RegionManager
import com.woxloi.custombiome.utils.Logger
import com.woxloi.custombiome.utils.Msg
import com.woxloi.custombiome.world.WorldManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class CustomBiomePlugin : JavaPlugin() {

    companion object {
        lateinit var instance: CustomBiomePlugin
            private set
    }

    private lateinit var database: BiomeDatabase

    override fun onLoad() {
        instance = this
        Logger.info("CustomBiome is loading...")
    }

    override fun onEnable() {
        try {
            saveDefaultConfig()
            reloadConfig()
            copyDefaultBiomes()

            Msg.init(config.getString("settings.prefix", "§e[§b§lCustomBiome§e] §r")!!)

            checkRequiredPlugins()

            database = setupDatabase()

            val biomesDir = File(dataFolder, config.getString("settings.biomes-dir", "biomes")!!)
            BiomeRegistry.loadFromDirectory(biomesDir)

            WorldManager.init(
                db     = database,
                prefix = config.getString("world.prefix", "cb_")!!
            )

            RegionManager.init(
                db         = database,
                autoCreate = config.getBoolean("worldguard.auto-create-region", true),
                prefix     = config.getString("worldguard.region-prefix", "cb_")!!
            )

            val cmd = CbiomeCommand()
            getCommand("cbiome")?.apply {
                setExecutor(cmd)
                tabCompleter = cmd
            }

            server.pluginManager.registerEvents(PlayerListener(database), this)
            server.pluginManager.registerEvents(WorldListener(), this)
            server.pluginManager.registerEvents(com.woxloi.custombiome.ui.GuiListener(), this)
            server.pluginManager.registerEvents(WandListener, this)

            Logger.success("CustomBiome has been fully enabled! (biomes: ${BiomeRegistry.count()})")

        } catch (e: Exception) {
            Logger.error("Failed to enable CustomBiome: ${e.message}")
            e.printStackTrace()
            server.pluginManager.disablePlugin(this)
        }
    }

    override fun onDisable() {
        Logger.warn("CustomBiome is disabling...")
        database.close()
        Logger.info("CustomBiome disabled.")
    }

    fun reloadPlugin() {
        reloadConfig()
        Msg.init(config.getString("settings.prefix", "§e[§b§lCustomBiome§e] §r")!!)
        BiomeRegistry.clear()
        val biomesDir = File(dataFolder, config.getString("settings.biomes-dir", "biomes")!!)
        BiomeRegistry.loadFromDirectory(biomesDir)
        Logger.info("CustomBiome reloaded. Biomes: ${BiomeRegistry.count()}")
    }

    private fun copyDefaultBiomes() {
        val biomesDir = File(dataFolder, "biomes")
        if (!biomesDir.exists()) {
            biomesDir.mkdirs()
            listOf("magic_forest.yml", "ancient_desert.yml", "coral_ocean.yml", "frozen_peaks.yml", "mesa_plateau.yml").forEach { name ->
                runCatching { saveResource("biomes/$name", false) }
                    .onFailure { Logger.warn("Could not copy default biome: $name") }
            }
        }
    }

    private fun setupDatabase(): BiomeDatabase {
        val cfg = config.getConfigurationSection("database")!!
        val provider = MySQLProvider(
            host     = cfg.getString("host", "localhost")!!,
            port     = cfg.getInt("port", 3306),
            database = cfg.getString("name", "custombiome")!!,
            username = cfg.getString("username", "root")!!,
            password = cfg.getString("password", "")!!,
            useSSL   = cfg.getBoolean("useSSL", false),
            poolSize = cfg.getInt("pool-size", 10)
        )
        // MySQL に失敗してもフラットファイルにフォールバックするので例外を投げない
        return BiomeDatabase.create(provider, dataFolder).also { it.init() }
    }

    private fun checkRequiredPlugins() {
        if (server.pluginManager.getPlugin("WorldEdit") == null)
            throw IllegalStateException("WorldEdit が見つかりません。プラグインを導入してください。")
        if (server.pluginManager.getPlugin("WorldGuard") == null)
            throw IllegalStateException("WorldGuard が見つかりません。プラグインを導入してください。")
        Logger.success("WorldEdit / WorldGuard を確認しました。")
    }
}
