package com.woxloi.custombiome.listener

import com.woxloi.custombiome.CustomBiomePlugin
import com.woxloi.custombiome.generator.FeaturePlacer
import com.woxloi.custombiome.utils.Logger
import com.woxloi.custombiome.world.WorldManager
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

        val chunk       = event.chunk
        val customWorld = WorldManager.getCustomWorld(chunk.world.name) ?: return
        val generator   = customWorld.generator ?: return

        // ChunkLoadEvent の中でブロックを直接置くと別チャンクのロードを誘発して
        // スタックオーバーフローになる場合があるため、1tick後に実行する。
        // メインスレッドのまま遅延させるので Bukkit API は安全に呼べる。
        CustomBiomePlugin.instance.server.scheduler.runTask(
            CustomBiomePlugin.instance,
            Runnable {
                // チャンクがまだロードされているか確認
                if (!chunk.isLoaded) return@Runnable

                val heightMap = Array(16) { x ->
                    IntArray(16) { z ->
                        generator.getHeightAt(chunk.x * 16 + x, chunk.z * 16 + z)
                    }
                }

                try {
                    FeaturePlacer.populate(chunk, customWorld.biome, heightMap, customWorld.seed)
                } catch (e: Exception) {
                    Logger.error("FeaturePlacer error at chunk (${chunk.x}, ${chunk.z}): ${e.message}")
                    e.printStackTrace()
                }
            }
        )
    }

    @EventHandler
    fun onWorldLoad(event: WorldLoadEvent) {
        if (WorldManager.isCustomWorld(event.world.name))
            Logger.info("Custom biome world '${event.world.name}' has been loaded.")
    }

    @EventHandler
    fun onWorldUnload(event: WorldUnloadEvent) {
        if (WorldManager.isCustomWorld(event.world.name))
            Logger.info("Custom biome world '${event.world.name}' has been unloaded.")
    }
}