package com.woxloi.custombiome.api

import com.woxloi.custombiome.biome.CustomBiome
import com.woxloi.custombiome.region.RegionManager
import com.woxloi.custombiome.world.CustomWorld
import com.woxloi.custombiome.world.WorldManager
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

/**
 * 他プラグインや内部モジュールが CustomBiome 機能を呼び出すための
 * 公開 API ファサード。BiomeRegistry / WorldManager / RegionManager に委譲する。
 */
object BiomeAPI {

    // ----------------------------------------------------------------
    // バイオーム情報
    // ----------------------------------------------------------------

    /** キーでバイオームを取得 */
    fun getBiome(key: String): CustomBiome? = BiomeRegistry.get(key)

    /** 登録済みバイオーム一覧 */
    fun getAllBiomes(): List<CustomBiome> = BiomeRegistry.getAll()

    /** 登録数 */
    fun getBiomeCount(): Int = BiomeRegistry.count()

    // ----------------------------------------------------------------
    // ワールド
    // ----------------------------------------------------------------

    /**
     * 指定バイオームでカスタムワールドを生成する。
     * @return 生成された World、失敗時は null
     */
    fun createWorld(
        biome: CustomBiome,
        worldName: String? = null,
        seed: Long = 0L,
        createdBy: String = "api"
    ): World? = WorldManager.createWorld(biome, worldName, seed, createdBy)

    /** カスタムワールドのメタデータを取得 */
    fun getCustomWorld(worldName: String): CustomWorld? = WorldManager.getCustomWorld(worldName)

    /** 全カスタムワールド一覧 */
    fun getAllCustomWorlds(): List<CustomWorld> = WorldManager.getAllWorlds()

    /** そのワールドがカスタム生成かどうか */
    fun isCustomWorld(worldName: String): Boolean = WorldManager.isCustomWorld(worldName)

    // ----------------------------------------------------------------
    // リージョン（WorldGuard）
    // ----------------------------------------------------------------

    /**
     * プレイヤーの現在地にある WG リージョンに
     * バイオームを割り当てる。
     */
    fun assignBiomeToRegion(
        player: Player,
        biome: CustomBiome
    ): Boolean = RegionManager.assignBiomeToSelection(player, biome)

    /** 指定 Location のバイオームキーを取得（リージョン割り当てベース） */
    fun getBiomeAt(location: Location): CustomBiome? =
        RegionManager.getBiomeAt(location)
}
