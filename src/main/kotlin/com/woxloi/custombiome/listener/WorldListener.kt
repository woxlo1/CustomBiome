package com.woxloi.custombiome.listener

import com.woxloi.custombiome.CustomBiomePlugin
import com.woxloi.custombiome.generator.FeaturePlacer
import com.woxloi.custombiome.utils.Logger
import com.woxloi.custombiome.world.WorldManager
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent

class WorldListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    fun onChunkLoad(event: ChunkLoadEvent) {
        if (!event.isNewChunk) return

        val chunk = event.chunk
        val world = chunk.world
        val worldName = world.name
        val customWorld = WorldManager.getCustomWorld(worldName) ?: return
        val generator = customWorld.generator ?: return

        // 非同期でチャンクを完全に読み込む
        world.getChunkAtAsync(chunk.x, chunk.z).thenAcceptAsync { loadedChunk ->
            val heightMap = Array(16) { x ->
                IntArray(16) { z ->
                    generator.getHeightAt(loadedChunk.x * 16 + x, loadedChunk.z * 16 + z)
                }
            }

            // ブロック操作は同期タスクで実行
            Bukkit.getScheduler().runTask(CustomBiomePlugin.instance, Runnable {
                try {
                    FeaturePlacer.populate(loadedChunk, customWorld.biome, heightMap, customWorld.seed)
                } catch (e: Exception) {
                    Logger.error("FeaturePlacer error at chunk (${loadedChunk.x}, ${loadedChunk.z}): ${e.message}")
                    e.printStackTrace()
                }
            })
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
