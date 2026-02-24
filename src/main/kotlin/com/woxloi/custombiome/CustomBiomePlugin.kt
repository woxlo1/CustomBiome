package com.woxloi.custombiome

import com.woxloi.custombiome.api.BiomeRegistry
import com.woxloi.custombiome.command.CbiomeCommand
import com.woxloi.custombiome.database.BiomeDatabase
import com.woxloi.custombiome.listener.PlayerListener
import com.woxloi.custombiome.listener.WorldListener
import com.woxloi.custombiome.region.RegionManager
import com.woxloi.custombiome.utils.Logger
import com.woxloi.custombiome.utils.Msg
import com.woxloi.custombiome.world.WorldManager
import com.woxloi.devapi.command.CommandRegistry
import com.woxloi.devapi.database.sql.MySQLProvider
import com.woxloi.devapi.hooks.HookManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class CustomBiomePlugin : JavaPlugin() {

    companion object {
        lateinit var instance: CustomBiomePlugin
            private set
    }

    private lateinit var database: BiomeDatabase

    // ----------------------------------------------------------------
    // ライフサイクル
    // ----------------------------------------------------------------

    override fun onLoad() {
        instance = this
        Logger.info("CustomBiome is loading...")
    }

    override fun onEnable() {
        try {
            // 設定ファイル
            saveDefaultConfig()
            reloadConfig()
            copyDefaultBiomes()

            // メッセージプレフィックスを config から初期化
            Msg.init(config.getString("settings.prefix", "§e[§b§lCustomBiome§e] §r")!!)

            // WoxloiDevAPI フック確認（WorldEdit / WorldGuard はここで検証）
            HookManager.initialize()
            checkRequiredHooks()

            // MySQL 初期化
            database = setupDatabase()

            // バイオームロード
            val biomesDir = File(dataFolder, config.getString("settings.biomes-dir", "biomes")!!)
            BiomeRegistry.loadFromDirectory(biomesDir)

            // WorldManager 初期化
            WorldManager.init(
                db     = database,
                prefix = config.getString("world.prefix", "cb_")!!
            )

            // RegionManager 初期化
            RegionManager.init(
                db          = database,
                autoCreate  = config.getBoolean("worldguard.auto-create-region", true),
                prefix      = config.getString("worldguard.region-prefix", "cb_")!!
            )

            // コマンド登録
            val root = CbiomeCommand.createRoot()
            CommandRegistry.registerRoot(root, listOf("cbiome"))

            // イベントリスナー登録
            server.pluginManager.registerEvents(PlayerListener(database), this)
            server.pluginManager.registerEvents(WorldListener(), this)

            Logger.success("CustomBiome has been fully enabled! (biomes: ${BiomeRegistry.count()})")

        } catch (e: Exception) {
            Logger.error("Failed to enable CustomBiome: ${e.message}")
            e.printStackTrace()
            server.pluginManager.disablePlugin(this)
        }
    }

    override fun onDisable() {
        Logger.warn("CustomBiome is disabling...")
        Logger.info("CustomBiome disabled.")
    }

    // ----------------------------------------------------------------
    // リロード（/cbiome reload から呼ばれる）
    // ----------------------------------------------------------------

    fun reloadPlugin() {
        reloadConfig()
        Msg.init(config.getString("settings.prefix", "§e[§b§lCustomBiome§e] §r")!!)
        BiomeRegistry.clear()
        val biomesDir = File(dataFolder, config.getString("settings.biomes-dir", "biomes")!!)
        BiomeRegistry.loadFromDirectory(biomesDir)
        Logger.info("CustomBiome reloaded. Biomes: ${BiomeRegistry.count()}")
    }

    // ----------------------------------------------------------------
    // 内部セットアップ
    // ----------------------------------------------------------------

    /** デフォルトバイオームYAMLをデータフォルダにコピー */
    private fun copyDefaultBiomes() {
        val biomesDir = File(dataFolder, "biomes")
        if (!biomesDir.exists()) {
            biomesDir.mkdirs()
            // JAR 内の biomes/ をコピー
            listOf("magic_forest.yml", "ancient_desert.yml").forEach { name ->
                runCatching { saveResource("biomes/$name", false) }
                    .onFailure { Logger.warn("Could not copy default biome: $name") }
            }
        }
    }

    /** MySQL プロバイダを作成して BiomeDatabase を初期化する */
    private fun setupDatabase(): BiomeDatabase {
        val cfg = config.getConfigurationSection("database")!!
        val provider = MySQLProvider(
            host      = cfg.getString("host", "localhost")!!,
            port      = cfg.getInt("port", 3306),
            database  = cfg.getString("name", "custombiome")!!,
            username  = cfg.getString("username", "root")!!,
            password  = cfg.getString("password", "")!!,
            useSSL    = cfg.getBoolean("useSSL", false)
        )
        val connected = provider.connect()
        if (!connected) {
            Logger.error("MySQL connection failed! Check database settings in config.yml.")
            throw IllegalStateException("MySQL connection failed.")
        }
        Logger.success("MySQL connected for CustomBiome.")
        return BiomeDatabase(provider).also { it.init() }
    }

    /** WorldEdit / WorldGuard が存在するか確認する */
    private fun checkRequiredHooks() {
        if (!HookManager.isHooked("WorldEdit")) {
            Logger.error("WorldEdit が見つかりません！プラグインを導入してください。")
            throw IllegalStateException("WorldEdit is required.")
        }
        if (!HookManager.isHooked("WorldGuard")) {
            Logger.error("WorldGuard が見つかりません！プラグインを導入してください。")
            throw IllegalStateException("WorldGuard is required.")
        }
        Logger.success("WorldEdit / WorldGuard フックに成功しました。")
    }
}
