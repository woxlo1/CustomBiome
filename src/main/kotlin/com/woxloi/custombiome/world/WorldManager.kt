package com.woxloi.custombiome.world

import com.woxloi.custombiome.biome.CustomBiome
import com.woxloi.custombiome.database.BiomeDatabase
import com.woxloi.custombiome.generator.CustomChunkGenerator
import com.woxloi.devapi.utils.Logger
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import java.util.concurrent.ConcurrentHashMap

/**
 * カスタムバイオームワールドの生成・取得・削除を一元管理する。
 * 生成済みワールドはメモリキャッシュと MySQL 両方に記録する。
 */
object WorldManager {

    /** worldName → CustomWorld のキャッシュ */
    private val worlds = ConcurrentHashMap<String, CustomWorld>()

    private lateinit var database: BiomeDatabase
    private var worldPrefix: String = "cb_"

    fun init(db: BiomeDatabase, prefix: String) {
        database = db
        worldPrefix = prefix
        loadFromDatabase()
    }

    // ----------------------------------------------------------------
    // ワールド生成
    // ----------------------------------------------------------------

    /**
     * 指定バイオームでカスタムワールドを生成する。
     * @param biome      使用するバイオーム
     * @param worldName  null の場合は prefix + biome.key で自動命名
     * @param seed       0 はランダム
     * @param createdBy  プレイヤー UUID 文字列
     * @return 生成された Bukkit World、失敗時は null
     */
    fun createWorld(
        biome: CustomBiome,
        worldName: String? = null,
        seed: Long = 0L,
        createdBy: String = "console"
    ): World? {
        val name      = worldName ?: "$worldPrefix${biome.key}_${System.currentTimeMillis() % 10000}"
        val finalSeed = if (seed == 0L) java.util.Random().nextLong() else seed

        if (Bukkit.getWorld(name) != null) {
            Logger.warn("World '$name' already exists.")
            return Bukkit.getWorld(name)
        }

        Logger.info("Generating custom world '$name' with biome '${biome.key}' (seed=$finalSeed)...")

        val generator = CustomChunkGenerator(biome, finalSeed)
        val creator   = WorldCreator(name)
            .generator(generator)
            .generateStructures(false)
            .seed(finalSeed)
            .type(WorldType.FLAT)  // ベースはFLAT、実際の地形はジェネレータが担当

        val world = runCatching { creator.createWorld() }.getOrElse { e ->
            Logger.error("Failed to create world '$name': ${e.message}")
            e.printStackTrace()
            return null
        } ?: return null

        val customWorld = CustomWorld(
            worldName  = name,
            biome      = biome,
            seed       = finalSeed,
            createdBy  = createdBy,
            generator  = generator
        )

        worlds[name] = customWorld
        database.saveWorld(customWorld)

        Logger.success("Custom world '$name' created successfully!")
        return world
    }

    // ----------------------------------------------------------------
    // 取得・検索
    // ----------------------------------------------------------------

    fun getCustomWorld(worldName: String): CustomWorld? = worlds[worldName]

    fun getAllWorlds(): List<CustomWorld> = worlds.values.toList()

    fun getWorldsByBiome(biomeKey: String): List<CustomWorld> =
        worlds.values.filter { it.biome.key == biomeKey }

    fun isCustomWorld(worldName: String): Boolean = worlds.containsKey(worldName)

    // ----------------------------------------------------------------
    // 削除
    // ----------------------------------------------------------------

    fun unregisterWorld(worldName: String) {
        worlds.remove(worldName)
        database.deleteWorld(worldName)
        Logger.info("Unregistered custom world '$worldName'.")
    }

    // ----------------------------------------------------------------
    // DB からキャッシュを復元
    // ----------------------------------------------------------------

    private fun loadFromDatabase() {
        val records = database.loadAllWorlds()
        var loaded = 0
        for (record in records) {
            val worldName = record["world_name"] as? String ?: continue
            val bukkit    = Bukkit.getWorld(worldName)
            if (bukkit != null) {
                // 既にロード済みのワールドは登録だけ
                worlds[worldName] = record.toCustomWorld() ?: continue
                loaded++
            }
        }
        Logger.info("Loaded $loaded custom world records from database.")
    }
}
