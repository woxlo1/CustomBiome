package com.woxloi.custombiome.listener

import com.woxloi.custombiome.generator.FeaturePlacer
import com.woxloi.custombiome.utils.Logger
import com.woxloi.custombiome.world.WorldManager
import com.woxloi.devapi.utils.scheduler.TaskScheduler
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent

/**
 * チャンク・ワールドのロードイベントを処理する。
 * - 新規チャンク: FeaturePlacer で地物を配置
 * - ワールドロード時: DB 記録と照合してカスタムワールドとして登録
 */
class WorldListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onChunkLoad(event: ChunkLoadEvent) {
        if (!event.isNewChunk) return

        val chunk     = event.chunk
        val worldName = chunk.world.name
        val customWorld = WorldManager.getCustomWorld(worldName) ?: return
        val generator   = customWorld.generator ?: return

        // 地形の高さマップを取得して地物を配置（メインスレッドで同期実行）
        val heightMap = generator.let {
            Array(16) { x -> IntArray(16) { z ->
                it.getHeightAt(chunk.x * 16 + x, chunk.z * 16 + z)
            }}
        }

        TaskScheduler.runSync {
            try {
                FeaturePlacer.populate(chunk, customWorld.biome, heightMap, customWorld.seed)
            } catch (e: Exception) {
                Logger.error("FeaturePlacer error at chunk (${chunk.x}, ${chunk.z}): ${e.message}")
                e.printStackTrace()
            }
        }
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        val worldName = event.world.name
        if (WorldManager.isCustomWorld(worldName)) {
            Logger.info("Custom biome world '$worldName' has been loaded.")
        }
    }

    @EventHandler
    fun onWorldUnload(event: WorldUnloadEvent) {
        val worldName = event.world.name
        if (WorldManager.isCustomWorld(worldName)) {
            Logger.info("Custom biome world '$worldName' has been unloaded.")
        }
    }
}
