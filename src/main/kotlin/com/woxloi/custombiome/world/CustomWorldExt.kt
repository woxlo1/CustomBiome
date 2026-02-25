package com.woxloi.custombiome.world

import com.woxloi.custombiome.api.BiomeRegistry

fun Map<String, Any>.toCustomWorld(): CustomWorld? {
    val worldName = this["world_name"] as? String ?: return null
    val biomeKey  = this["biome_key"]  as? String ?: return null
    val seed      = (this["seed"] as? Number)?.toLong() ?: 0L
    val createdBy = this["created_by"] as? String ?: "unknown"
    val createdAt = (this["created_at"] as? Number)?.toLong() ?: 0L
    val biome     = BiomeRegistry.get(biomeKey) ?: return null
    return CustomWorld(worldName, biome, seed, createdBy, createdAt, null)
}
