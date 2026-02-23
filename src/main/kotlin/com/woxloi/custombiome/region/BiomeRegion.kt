package com.woxloi.custombiome.region

import com.woxloi.custombiome.biome.CustomBiome

/**
 * WorldGuard リージョンとバイオームの紐付けを保持するデータクラス。
 */
data class BiomeRegion(
    /** WorldGuard のリージョンID */
    val regionId: String,

    /** リージョンが存在するワールド名 */
    val worldName: String,

    /** 割り当てられたカスタムバイオーム */
    val biome: CustomBiome,

    /** 割り当てた操作者の UUID 文字列 */
    val assignedBy: String,

    /** 割り当て日時（エポック秒） */
    val assignedAt: Long = System.currentTimeMillis() / 1000L
) {
    fun toDbMap(): Map<String, Any> = mapOf(
        "region_id"   to regionId,
        "world_name"  to worldName,
        "biome_key"   to biome.key,
        "assigned_by" to assignedBy,
        "assigned_at" to assignedAt
    )
}
