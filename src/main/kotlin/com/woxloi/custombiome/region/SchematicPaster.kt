package com.woxloi.custombiome.region

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import com.woxloi.custombiome.utils.Logger
import org.bukkit.Location
import java.io.File
import java.io.FileInputStream

object SchematicPaster {

    fun paste(file: File, location: Location, ignoreAir: Boolean = true): Boolean {
        if (!file.exists()) { Logger.error("Schematic file not found: ${file.absolutePath}"); return false }
        val format = ClipboardFormats.findByFile(file)
        if (format == null) { Logger.error("Unknown schematic format: ${file.name}"); return false }

        return try {
            val clipboard: Clipboard = FileInputStream(file).use { format.getReader(it).read() }
            val weWorld   = BukkitAdapter.adapt(location.world!!)
            val origin    = BlockVector3.at(location.blockX, location.blockY, location.blockZ)
            WorldEdit.getInstance().newEditSession(weWorld).use { editSession ->
                val operation = ClipboardHolder(clipboard).createPaste(editSession)
                    .to(origin).ignoreAirBlocks(ignoreAir).build()
                Operations.complete(operation)
            }
            Logger.success("Pasted schematic '${file.name}' at (${location.blockX}, ${location.blockY}, ${location.blockZ}).")
            true
        } catch (e: Exception) {
            Logger.error("Failed to paste schematic '${file.name}': ${e.message}")
            e.printStackTrace()
            false
        }
    }

    fun pasteByName(schematicName: String, schematicsDir: File, location: Location, ignoreAir: Boolean = true): Boolean {
        val file = File(schematicsDir, "$schematicName.schem").takeIf { it.exists() }
            ?: File(schematicsDir, "$schematicName.schematic").takeIf { it.exists() }
            ?: run { Logger.error("Schematic '$schematicName' not found in ${schematicsDir.name}/"); return false }
        return paste(file, location, ignoreAir)
    }
}
