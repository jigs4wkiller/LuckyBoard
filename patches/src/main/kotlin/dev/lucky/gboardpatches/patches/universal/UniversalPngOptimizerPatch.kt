package dev.lucky.gboardpatches.patches.universal

import app.morphe.patcher.patch.resourcePatch
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * Universal PNG Optimizer - PURE JDK IMPLEMENTATION (no external libraries whatsoever).
 *
 * Goal: Provide structural PNG optimization without any ext lib (no pngtastic, no AWT, no javax.imageio).
 * This completely eliminates the "ClassNotFound com.googlecode.pngtastic.ant.PngOptimizerTask" problem
 * that made the bundle "corrupted" in Morphe Manager.
 *
 * Technique (minimal but effective, self-contained):
 * - Parse PNG chunks (IHDR not even needed for basic re-compress).
 * - Collect all IDAT data, decompress to get the filtered image stream.
 * - Re-compress the stream with Deflater(BEST_COMPRESSION).
 * - Rebuild the PNG with a single new IDAT chunk + correct CRCs/lengths.
 * - Only replace the file if the result is strictly smaller.
 *
 * This is "optipng-like" in spirit (better compression on the data) but without full filter enumeration
 * or pixel-level work (those would require a full decoder and are left for desktop tools anyway).
 *
 * The previous pngtastic version is completely replaced. No stripping, no extra deps in the .mpp.
 *
 * UNIVERSAL, safe for 9-patches, reports stats, same exclusion logic as before.
 */
internal val universalPngOptimizerPatch = resourcePatch(
    name = "Universal PNG Optimizer",
    description = "Optimizes PNG and *.9.png using 100% pure JDK (Deflater + CRC32 only). No external libs at all. Re-compresses IDAT data for smaller size. Safe for 9-patches. UNIVERSAL - works on any app.",
    default = true
) {
    finalize {
        val resDir = get("res") as? File
        if (resDir == null || !resDir.exists() || !resDir.isDirectory) {
            println("  [UniversalPngOptimizer] No res/ directory found.")
            return@finalize
        }
        val root = resDir.parentFile ?: resDir
        if (!root.exists() || !root.isDirectory) {
            println("  [UniversalPngOptimizer] No valid root.")
            return@finalize
        }

        println("  PngOptimizer (pure-JDK) working...")
        println("  Target: ${root.absolutePath}")

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
            .filter { f -> f.isFile && f.name.endsWith(".png", ignoreCase = true) && !f.name.endsWith(".9.png", ignoreCase = true) && !isExcluded(f.invariantSeparatorsPath) }
            .toList()

        val ninePngs = root.walkTopDown()
            .filter { f -> f.isFile && f.name.endsWith(".9.png", ignoreCase = true) && !isExcluded(f.invariantSeparatorsPath) }
            .toList()

        println("  Found ${regularPngs.size} regular PNG(s), ${ninePngs.size} 9.png file(s).")

        var processed = 0
        regularPngs.forEach { if (optimizePngPure(it)) processed++ }
        ninePngs.forEach { if (optimizePngPure(it)) processed++ }

        val sAfter = calculateTotalSize(root)
        val freed = (sBefore - sAfter) / 1024

        println("  Done (pure JDK)! Processed: $processed, Freed: $freed Kb")
        println("  (No external libs - fully self-contained in the .mpp)")
    }
}

private fun calculateTotalSize(dir: File): Long = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }

/** Pure JDK PNG re-compressor. Returns true if file was made smaller. */
private fun optimizePngPure(file: File): Boolean {
    val orig = file.readBytes()
    if (orig.size < 8 || !isPngSig(orig)) return false

    try {
        val chunks = parseChunks(orig)
        val idatBytes = ByteArrayOutputStream()
        chunks.filter { it.type == "IDAT" }.forEach { idatBytes.write(it.data) }
        if (idatBytes.size() == 0) return false

        val compressed = idatBytes.toByteArray()

        // Decompress
        val inf = Inflater()
        inf.setInput(compressed)
        val raw = ByteArrayOutputStream()
        val buf = ByteArray(4096)
        while (!inf.finished()) {
            val n = inf.inflate(buf)
            if (n > 0) raw.write(buf, 0, n)
        }
        inf.end()
        val rawData = raw.toByteArray()
        if (rawData.isEmpty()) return false

        // Re-compress best
        val def = Deflater(Deflater.BEST_COMPRESSION, true)
        def.setInput(rawData)
        def.finish()
        val newComp = ByteArrayOutputStream()
        while (!def.finished()) {
            val n = def.deflate(buf)
            if (n > 0) newComp.write(buf, 0, n)
        }
        def.end()
        val newIdat = newComp.toByteArray()

        if (newIdat.size >= compressed.size) return false

        // Rebuild with one IDAT
        val newChunks = mutableListOf<Chunk>()
        var replaced = false
        chunks.forEach { c ->
            if (c.type == "IDAT") {
                if (!replaced) { newChunks += Chunk("IDAT", newIdat); replaced = true }
            } else {
                newChunks += c
            }
        }
        if (!replaced) newChunks += Chunk("IDAT", newIdat)

        val rebuilt = buildPng(newChunks)
        if (rebuilt.size < orig.size) {
            file.writeBytes(rebuilt)
            return true
        }
    } catch (_: Exception) {
        // safe: keep original on any error
    }
    return false
}

private data class Chunk(val type: String, val data: ByteArray)

private fun isPngSig(b: ByteArray): Boolean {
    val sig = byteArrayOf(0x89.toByte(),0x50,0x4E,0x47,0x0D,0x0A,0x1A,0x0A)
    return b.size >= 8 && sig.indices.all { b[it] == sig[it] }
}

private fun parseChunks(b: ByteArray): List<Chunk> {
    val out = mutableListOf<Chunk>()
    var p = 8
    while (p + 12 <= b.size) {
        val len = ((b[p].toInt() and 0xFF) shl 24) or ((b[p+1].toInt() and 0xFF) shl 16) or ((b[p+2].toInt() and 0xFF) shl 8) or (b[p+3].toInt() and 0xFF)
        if (p + 12 + len > b.size) break
        val type = String(b, p+4, 4, Charsets.US_ASCII)
        val data = if (len > 0) b.copyOfRange(p+8, p+8+len) else ByteArray(0)
        out.add(Chunk(type, data))
        p += 12 + len
        if (type == "IEND") break
    }
    return out
}

private fun buildPng(chunks: List<Chunk>): ByteArray {
    val out = ByteArrayOutputStream()
    out.write(byteArrayOf(0x89.toByte(),0x50,0x4E,0x47,0x0D,0x0A,0x1A,0x0A))
    val crc32 = CRC32()
    for (c in chunks) {
        val tb = c.type.toByteArray(Charsets.US_ASCII)
        val len = c.data.size
        out.write((len ushr 24) and 0xFF); out.write((len ushr 16) and 0xFF); out.write((len ushr 8) and 0xFF); out.write(len and 0xFF)
        out.write(tb)
        out.write(c.data)
        crc32.reset(); crc32.update(tb); crc32.update(c.data)
        val cv = crc32.value.toInt()
        out.write((cv ushr 24) and 0xFF); out.write((cv ushr 16) and 0xFF); out.write((cv ushr 8) and 0xFF); out.write(cv and 0xFF)
    }
    return out.toByteArray()
}
