package com.woxloi.custombiome.listener

import com.woxloi.custombiome.utils.Msg
import com.woxloi.custombiome.utils.WandItemUtil
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * /cbiome wand で付与した CustomBiome ワンドによる範囲選択を処理する。
 * 左クリック → pos1、右クリック → pos2。
 *
 * 判定は WandItemUtil.isWand() で行い、PDC タグのないただの木の斧には反応しない。
 */
object WandListener : Listener {

    data class Selection(
        val world: String,
        val pos1: Triple<Int, Int, Int>? = null,
        val pos2: Triple<Int, Int, Int>? = null
    ) {
        fun isComplete() = pos1 != null && pos2 != null
    }

    private val selections = ConcurrentHashMap<UUID, Selection>()

    fun getSelection(uuid: UUID): Selection? = selections[uuid]
    fun clearSelection(uuid: UUID) { selections.remove(uuid) }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player

        // PDC タグで本物の CustomBiome ワンドかどうかを確認
        if (!WandItemUtil.isWand(player.inventory.itemInMainHand)) return

        val block = event.clickedBlock ?: return

        when (event.action) {
            Action.LEFT_CLICK_BLOCK -> {
                event.isCancelled = true
                val prev = selections[player.uniqueId]
                selections[player.uniqueId] = Selection(
                    world = block.world.name,
                    pos1  = Triple(block.x, block.y, block.z),
                    pos2  = prev?.takeIf { it.world == block.world.name }?.pos2
                )
                Msg.send(player, "&aPos1 を設定: &e(${block.x}, ${block.y}, ${block.z})")
            }
            Action.RIGHT_CLICK_BLOCK -> {
                event.isCancelled = true
                val prev = selections[player.uniqueId]
                selections[player.uniqueId] = Selection(
                    world = block.world.name,
                    pos1  = prev?.takeIf { it.world == block.world.name }?.pos1,
                    pos2  = Triple(block.x, block.y, block.z)
                )
                Msg.send(player, "&aPos2 を設定: &e(${block.x}, ${block.y}, ${block.z})")
            }
            else -> return
        }

        val sel = selections[player.uniqueId]
        if (sel?.isComplete() == true) {
            Msg.send(player, "&b範囲選択完了！ &7/cbiome region set <biome_key> で登録してください。")
        }
    }
}
