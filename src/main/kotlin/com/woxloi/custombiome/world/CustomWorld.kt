package com.woxloi.custombiome.world

import com.woxloi.custombiome.biome.CustomBiome
import com.woxloi.custombiome.generator.CustomChunkGenerator

/**
 * カスタムバイオームで生成されたワールドのメタデータを保持するデータクラス。
 * MySQL に永続化する際のモデルにもなる。
 */
data class CustomWorld(
    /** Bukkit ワールド名 */
    val worldName: String,

    /** 割り当てられたバイオーム */
    val biome: CustomBiome,

    /** 生成シード */
    val seed: Long,

    /** 生成したプレイヤーの UUID 文字列 */
    val createdBy: String,

    /** 生成日時（エポック秒） */
    val createdAt: Long = System.currentTimeMillis() / 1000L,

    /** チャンクジェネレータ参照（DB には保存しない） */
    @Transient val generator: CustomChunkGenerator? = null
) {
    /** DB 保存用にシリアライズしたマップを返す */
    fun toDbMap(): Map<String, Any> = mapOf(
        "world_name"  to worldName,
        "biome_key"   to biome.key,
        "seed"        to seed,
        "created_by"  to createdBy,
        "created_at"  to createdAt
    )
}
