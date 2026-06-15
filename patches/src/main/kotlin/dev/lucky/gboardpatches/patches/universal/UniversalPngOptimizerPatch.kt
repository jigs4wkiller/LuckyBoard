package dev.lucky.gboardpatches.patches.universal

import app.morphe.patcher.patch.resourcePatch
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * Universal PNG Optimizer patch - NEW IMPLEMENTATION (no external libs).
 *
 * Pure Java / JDK only (java.util.zip.Deflater/Inflater, no pngtastic, no AWT, no javax.imageio).
 * This avoids the ant class loading problems that broke the bundle in Morphe Manager.
 *
 * What it does (basic structural optimization like a very light optipng):
 * - Walks PNG and 9.png files (same exclusions as before).
 * - For each, parses chunks, collects+deflates IDAT data with BEST_COMPRESSION.
 * - Rebuilds the PNG with the re-compressed IDAT (single chunk for simplicity).
 * - Replaces the file only if smaller.
 * - Reports stats.
 *
 * Limitations (by design for no-ext-lib + small code):
 * - Does not re-try all PNG filters (that would require full pixel decode + per-row filter choice).
 * - No color quantization / palette reduction (that is lossy and best done with pngquant on desktop anyway).
 * - Gains are mainly from better zlib compression level/strategy on the existing filtered data.
 *
 * For maximum size reduction, combine with desktop tools (pngquant + optipng/zopflipng) as before.
 *
 * UNIVERSAL: no compatibleWith() so it applies to any app in Morphe.
 */
internal val universalPngOptimizerPatch = resourcePatch(
    name = "Universal PNG Optimizer",
    description = "Optimizes PNG and *.9.png files using pure-JDK only (no external libs, no ant baggage). Re-compresses IDAT data with best Deflater. Safe for 9-patches. UNIVERSAL patch - works on any app in Morphe. (New impl to avoid previous load issues.)",
    default = true
) {
    // No compatibleWith() -> this is a universal patch applicable to all apps in Morphe.

    finalize {
        val resDir = get("res") as? File
        if (resDir == null || !resDir.exists() || !resDir.isDirectory) {
            println("  [UniversalPngOptimizer] No res/ directory found in decompile context.")
            return@finalize
        }
        val root = resDir.parentFile ?: resDir
        if (!root.exists() || !root.isDirectory) {
            println("  [UniversalPngOptimizer] No valid decompile root directory found.")
            return@finalize
        }

        println("  PngOptimizer (pure-JDK new impl) working...")
        println("  Target directory: \"${root.absolutePath}\"")

        val startAt = java.time.LocalTime.now().toString()
        val sBefore = calculateTotalSize(root)

        val excludedDirs = listOf("build", "original", "smali", "kotlin")
        fun isExcluded(path: String): Boolean {
            val p = path.replace('\\', '/')
            return excludedDirs.any { ex ->
                p.contains("/$ex/") || p.startsWith("$ex/") || p.startsWith("./$ex/") || p == ex
            }
        }

        val regularPngs = root.walkTopDown()
            .filter { f ->
                f.isFile &&
                    f.name.endsWith(".png", ignoreCase = true) &&
                    !f.name.endsWith(".9.png", ignoreCase = true) &&
                    !isExcluded(f.invariantSeparatorsPath)
            }
            .toList()

        val ninePngs = root.walkTopDown()
            .filter { f ->
                f.isFile &&
                    f.name.endsWith(".9.png", ignoreCase = true) &&
                    !isExcluded(f.invariantSeparatorsPath)
            }
            .toList()

        val victimPngArr = regularPngs.size
        val victimPng9Arr = ninePngs.size

        if (victimPngArr > 0) {
            println("  Found ${victimPngArr} regular PNG(s).")
        } else {
            println("  No regular png files found!")
        }
        if (victimPng9Arr > 0) {
            println("  Found ${victimPng9Arr} 9.png file(s). (safe for 9-patch borders)")
        } else {
            println("  No 9.png files found!")
        }

        if (victimPngArr == 0 && victimPng9Arr == 0) {
            println("  Done (nothing to do).")
            return@finalize
        }

        var processed = 0
        regularPngs.forEach { f ->
            if (optimizePngFilePure(f)) processed++
        }
        ninePngs.forEach { f ->
            if (optimizePngFilePure(f)) processed++
        }

        val sAfter = calculateTotalSize(root)
        val freed = (sBefore - sAfter) / 1024

        println("  Done!")
        println("  Results:")
        println("      Start time: $startAt        End time: ${java.time.LocalTime.now()}")
        println("      Files processed:")
        println("             png| $victimPngArr")
        println("           9.png| $victimPng9Arr")
        println("      Optimized: $processed")
        println("           Freed: $freed Kb")
        println("  Note: Pure-JDK only (Deflater re-compress on IDAT). No external libs. For max gains use desktop pngquant+optipng.")
    }
}

private fun calculateTotalSize(dir: File): Long {
    return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
}

/**
 * Pure-JDK PNG optimizer (no external libraries at all).
 * Re-compresses the IDAT data with best Deflater compression.
 * This is a structural optimization (similar in spirit to a very light optipng).
 * Returns true if the file was replaced with a smaller version.
 */
private fun optimizePngFilePure(file: File): Boolean {
    val original = file.readBytes()
    if (original.size < 8 || !isPngSignature(original)) {
        return false
    }

    try {
        val chunks = parseChunks(original)
        if (chunks.isEmpty()) return false

        // Collect all IDAT data
        val idatData = ByteArrayOutputStream()
        chunks.filter { it.type == "IDAT" }.forEach { idatData.write(it.data) }
        if (idatData.size() == 0) return false

        val compressedIdat = idatData.toByteArray()

        // Decompress
        val inflater = java.util.zip.Inflater()
        inflater.setInput(compressedIdat)
        val rawImageData = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (!inflater.finished()) {
            val count = inflater.inflate(buffer)
            if (count > 0) rawImageData.write(buffer, 0, count)
        }
        inflater.end()
        val raw = rawImageData.toByteArray()
        if (raw.isEmpty()) return false

        // Re-compress with best settings (pure JDK)
        val deflater = Deflater(Deflater.BEST_COMPRESSION, true) // true = no zlib header for raw deflate
        deflater.setInput(raw)
        deflater.finish()
        val newCompressed = ByteArrayOutputStream()
        while (!deflater.finished()) {
            val count = deflater.deflate(buffer)
            if (count > 0) newCompressed.write(buffer, 0, count)
        }
        deflater.end()
        val newIdatData = newCompressed.toByteArray()

        // If no gain or larger, keep original
        if (newIdatData.size >= compressedIdat.size) {
            return false
        }

        // Rebuild PNG with single new IDAT chunk (simple and safe)
        val newChunks = mutableListOf<Chunk>()
        var idatReplaced = false
        for (chunk in chunks) {
            if (chunk.type == "IDAT") {
                if (!idatReplaced) {
                    newChunks.add(Chunk("IDAT", newIdatData))
                    idatReplaced = true
                }
                // skip other IDATs (we merged)
            } else {
                newChunks.add(chunk)
            }
        }
        if (!idatReplaced) {
            newChunks.add(Chunk("IDAT", newIdatData))
        }

        val newPng = buildPng(newChunks)
        if (newPng.size < original.size) {
            file.writeBytes(newPng)
            return true
        }
    } catch (e: Exception) {
        // Any parse/compress error -> keep original (safe)
        // println("    Skipped ${file.name} (pure impl): ${e.javaClass.simpleName}")
    }
    return false
}

private data class Chunk(val type: String, val data: ByteArray)

private fun isPngSignature(data: ByteArray): Boolean {
    val sig = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    if (data.size < sig.size) return false
    for (i in sig.indices) {
        if (data[i] != sig[i]) return false
    }
    return true
}

private fun parseChunks(data: ByteArray): List<Chunk> {
    val chunks = mutableListOf<Chunk>()
    var pos = 8 // skip signature
    while (pos + 8 <= data.size) {
        val length = ((data[pos].toInt() and 0xFF) shl 24) or
                     ((data[pos+1].toInt() and 0xFF) shl 16) or
                     ((data[pos+2].toInt() and 0xFF) shl 8) or
                     (data[pos+3].toInt() and 0xFF)
        if (pos + 12 + length > data.size) break
        val typeBytes = data.copyOfRange(pos+4, pos+8)
        val type = String(typeBytes, Charsets.US_ASCII)
        val chunkData = data.copyOfRange(pos+8, pos+8+length)
        // CRC is at pos+8+length .. pos+12+length, we trust it for our purposes
        chunks.add(Chunk(type, chunkData))
        pos += 12 + length
        if (type == "IEND") break
    }
    return chunks
}

private fun buildPng(chunks: List<Chunk>): ByteArray {
    val out = ByteArrayOutputStream()
    // Signature
    out.write(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
    for (chunk in chunks) {
        val typeBytes = chunk.type.toByteArray(Charsets.US_ASCII)
        val len = chunk.data.size
        // length (big endian)
        out.write((len ushr 24) and 0xFF)
        out.write((len ushr 16) and 0xFF)
        out.write((len ushr 8) and 0xFF)
        out.write(len and 0xFF)
        // type
        out.write(typeBytes)
        // data
        out.write(chunk.data)
        // CRC (type + data)
        val crc = CRC32()
        crc.update(typeBytes)
        crc.update(chunk.data)
        val crcVal = crc.value.toInt()
        out.write((crcVal ushr 24) and 0xFF)
        out.write((crcVal ushr 16) and 0xFF)
        out.write((crcVal ushr 8) and 0xFF)
        out.write(crcVal and 0xFF)
    }
    return out.toByteArray()
}
ENDOFFILE
echo "PNG patch source updated to pure-JDK only implementation (no ext libs)."
