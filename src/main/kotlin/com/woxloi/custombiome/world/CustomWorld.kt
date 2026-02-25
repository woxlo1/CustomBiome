package com.woxloi.custombiome.world

import com.woxloi.custombiome.biome.CustomBiome
import com.woxloi.custombiome.generator.CustomChunkGenerator

data class CustomWorld(
    val worldName: String,
    val biome: CustomBiome,
    val seed: Long,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis() / 1000L,
    @Transient val generator: CustomChunkGenerator? = null
) {
    fun toDbMap(): Map<String, Any> = mapOf(
        "world_name" to worldName,
        "biome_key"  to biome.key,
        "seed"       to seed,
        "created_by" to createdBy,
        "created_at" to createdAt
    )
}
