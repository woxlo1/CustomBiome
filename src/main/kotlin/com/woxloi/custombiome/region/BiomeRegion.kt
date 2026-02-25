package com.woxloi.custombiome.region

import com.woxloi.custombiome.biome.CustomBiome

data class BiomeRegion(
    val regionId: String,
    val worldName: String,
    val biome: CustomBiome,
    val assignedBy: String,
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
