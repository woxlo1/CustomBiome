package com.woxloi.custombiome.listener

import com.woxloi.custombiome.api.BiomeAPI
import com.woxloi.custombiome.biome.CustomBiome
import com.woxloi.custombiome.database.BiomeDatabase
import com.woxloi.devapi.utils.Logger
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * プレイヤーの移動を監視し、バイオームへの入退場を検出する。
 * - アンビエンスパーティクル・サウンド再生
 * - 訪問履歴を MySQL に記録
 */
class PlayerListener(private val database: BiomeDatabase) : Listener {

    /** UUID → 現在いるバイオームキー */
    private val currentBiome = ConcurrentHashMap<UUID, String>()

    // ----------------------------------------------------------------
    // 移動監視（チャンク境界でのみ重い処理を実行）
    // ----------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onMove(event: PlayerMoveEvent) {
        // ブロック移動がなければスキップ（軽量化）
        val from = event.from
        val to   = event.to ?: return
        if (from.blockX == to.blockX && from.blockZ == to.blockZ) return

        val player = event.player
        val biome  = BiomeAPI.getBiomeAt(to)

        val prevKey = currentBiome[player.uniqueId]
        val newKey  = biome?.key

        if (prevKey != newKey) {
            // バイオーム変化
            if (prevKey != null) onLeaveBiome(player, prevKey)
            if (biome != null)   onEnterBiome(player, biome)
            currentBiome[player.uniqueId] = newKey ?: return.also { currentBiome.remove(player.uniqueId) }
        }

        // アンビエンスパーティクル（確率で毎移動ごとに再生）
        if (biome != null) tickAmbience(player, biome)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        // ログイン時の現在バイオームを初期化
        val biome = BiomeAPI.getBiomeAt(event.player.location)
        if (biome != null) currentBiome[event.player.uniqueId] = biome.key
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        currentBiome.remove(event.player.uniqueId)
    }

    // ----------------------------------------------------------------
    // バイオーム入退場
    // ----------------------------------------------------------------

    private fun onEnterBiome(player: Player, biome: CustomBiome) {
        player.sendActionBar(
            net.kyori.adventure.text.Component.text("§a${biome.displayName} §7に入りました")
        )
        // 訪問履歴を記録（非同期）
        val worldName = player.world.name
        val uuid      = player.uniqueId.toString()
        com.woxloi.devapi.utils.scheduler.TaskScheduler.runAsync {
            database.recordVisit(uuid, biome.key, worldName)
        }
        Logger.info("${player.name} entered biome '${biome.key}'.")
    }

    private fun onLeaveBiome(player: Player, biomeKey: String) {
        Logger.info("${player.name} left biome '$biomeKey'.")
    }

    // ----------------------------------------------------------------
    // アンビエンスエフェクト
    // ----------------------------------------------------------------

    private fun tickAmbience(player: Player, biome: CustomBiome) {
        val ambience = biome.ambience

        // パーティクル
        if (ambience.particles.enabled) {
            val rng = java.util.Random()
            if (rng.nextDouble() < ambience.particles.density) {
                val particle = runCatching {
                    Particle.valueOf(ambience.particles.type.uppercase())
                }.getOrNull() ?: Particle.ENCHANTMENT_TABLE

                val loc = player.location.clone().add(
                    (rng.nextDouble() - 0.5) * 4,
                    rng.nextDouble() * 2,
                    (rng.nextDouble() - 0.5) * 4
                )
                player.spawnParticle(particle, loc, 1, 0.0, 0.0, 0.0, 0.0)
            }
        }

        // サウンド
        for (soundEntry in ambience.sounds) {
            if (java.util.Random().nextDouble() < soundEntry.chance) {
                val sound = runCatching {
                    Sound.valueOf(soundEntry.sound.uppercase())
                }.getOrNull() ?: continue
                player.playSound(player.location, sound, 0.5f, 1.0f)
            }
        }
    }
}
