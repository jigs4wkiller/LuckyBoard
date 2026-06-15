package dev.lucky.gboardpatches.patches.universal

import app.morphe.patcher.patch.resourcePatch
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * Universal PNG Optimizer - PURE JDK IMPLEMENTATION (NO EXTERNAL LIBS).
 *
 * This replaces the old pngtastic version completely.
 * Goal: provide useful PNG/9-patch size reduction inside Morphe without ever
 * pulling in pngtastic (and its ant/ subpackage that caused the ClassNotFound /
 * "corrupted bundle" crash in the manager).
 *
 * How it works (self-contained, only JDK):
 * - Same discovery logic as before (excludes build/, original/, smali*, kotlin*).
 * - Minimal PNG chunk parser.
 * - Collects all IDAT data, decompresses it.
 * - Re-compresses the raw stream with Deflater(BEST_COMPRESSION).
 * - Rebuilds the PNG with one new IDAT chunk and correct CRCs/lengths.
 * - Only overwrites the file if the result is strictly smaller.
 *
 * This is "structural" optimization (better compression on existing filtered data).
 * It does NOT do full filter re-selection or lossy quantization (those are best
 * left to desktop pngquant + optipng anyway, as noted in the original description).
 *
 * The patch remains UNIVERSAL and safe for 9-patches.
 */
internal val universalPngOptimizerPatch = resourcePatch(
    name = "Universal PNG Optimizer",
    description = "Optimizes PNG and *.9.png using only JDK classes (Deflater + CRC32). No external libraries. Re-compresses IDAT data for smaller files. Safe for 9-patches. UNIVERSAL patch.",
    default = true
) {
    finalize {
        val resDir = get("res") as? File
        if (resDir == null || !resDir.exists() || !resDir.isDirectory) {
            println("  [UniversalPngOptimizer] No res/ directory.")
            return@finalize
        }
        val root = resDir.parentFile ?: resDir
        if (!root.exists() || !root.isDirectory) {
            println("  [UniversalPngOptimizer] No valid root.")
            return@finalize
        }

        println("  PngOptimizer (pure JDK, no ext libs) working...")
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
            .filter { f ->
                f.isFile &&
                f.name.endsWith(".png", ignoreCase = true) &&
                !f.name.endsWith(".9.png", ignoreCase = true) &&
                !isExcluded(f.invariantSeparatorsPath)
            }.toList()

        val ninePngs = root.walkTopDown()
            .filter { f ->
                f.isFile &&
                f.name.endsWith(".9.png", ignoreCase = true) &&
                !isExcluded(f.invariantSeparatorsPath)
            }.toList()

        println("  Found ${regularPngs.size} regular PNG(s), ${ninePngs.size} 9.png file(s).")

        var processed = 0
        regularPngs.forEach { if (optimizePngPure(it)) processed++ }
        ninePngs.forEach { if (optimizePngPure(it)) processed++ }

        val sAfter = calculateTotalSize(root)
        val freed = (sBefore - sAfter) / 1024

        println("  Done (pure JDK)! Processed: $processed, freed: $freed Kb")
        println("  (No external libs - the implementation is fully self-contained.)")
    }
}

private fun calculateTotalSize(dir: File): Long =
    dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }

/** Pure JDK IDAT re-compressor. Returns true if the file was made smaller. */
private fun optimizePngPure(file: File): Boolean {
    val orig = file.readBytes()
    if (orig.size < 8 || !isPngSig(orig)) return false

    try {
        val chunks = parseChunks(orig)
        val idatOut = ByteArrayOutputStream()
        chunks.filter { it.type == "IDAT" }.forEach { idatOut.write(it.data) }
        if (idatOut.size() == 0) return false

        val comp = idatOut.toByteArray()

        // decompress
        val inf = Inflater()
        inf.setInput(comp)
        val raw = ByteArrayOutputStream()
        val buf = ByteArray(4096)
        while (!inf.finished()) {
            val n = inf.inflate(buf)
            if (n > 0) raw.write(buf, 0, n)
        }
        inf.end()
        val rawData = raw.toByteArray()
        if (rawData.isEmpty()) return false

        // re-compress best
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

        if (newIdat.size >= comp.size) return false

        // rebuild PNG with single IDAT
        val newChunks = mutableListOf<Chunk>()
        var replaced = false
        chunks.forEach { c ->
            if (c.type == "IDAT") {
                if (!replaced) {
                    newChunks += Chunk("IDAT", newIdat)
                    replaced = true
                }
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
        // keep original on any error - safe
    }
    return false
}

private data class Chunk(val type: String, val data: ByteArray)

private fun isPngSig(b: ByteArray): Boolean {
    val sig = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    return b.size >= 8 && sig.indices.all { b[it] == sig[it] }
}

private fun parseChunks(b: ByteArray): List<Chunk> {
    val out = mutableListOf<Chunk>()
    var p = 8
    while (p + 12 <= b.size) {
        val len = ((b[p].toInt() and 0xff) shl 24) or
                  ((b[p+1].toInt() and 0xff) shl 16) or
                  ((b[p+2].toInt() and 0xff) shl 8) or
                  (b[p+3].toInt() and 0xff)
        if (p + 12 + len > b.size) break
        val typ = String(b, p + 4, 4, Charsets.US_ASCII)
        val dat = if (len == 0) ByteArray(0) else b.copyOfRange(p + 8, p + 8 + len)
        out += Chunk(typ, dat)
        p += 12 + len
        if (typ == "IEND") break
    }
    return out
}

private fun buildPng(chunks: List<Chunk>): ByteArray {
    val out = ByteArrayOutputStream()
    out.write(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
    val crc = CRC32()
    for (c in chunks) {
        val tb = c.type.toByteArray(Charsets.US_ASCII)
        val l = c.data.size
        out.write((l ushr 24) and 0xff); out.write((l ushr 16) and 0xff)
        out.write((l ushr 8) and 0xff); out.write(l and 0xff)
        out.write(tb)
        out.write(c.data)
        crc.reset(); crc.update(tb); crc.update(c.data)
        val cv = crc.value.toInt()
        out.write((cv ushr 24) and 0xff); out.write((cv ushr 16) and 0xff)
        out.write((cv ushr 8) and 0xff); out.write(cv and 0xff)
    }
    return out.toByteArray()
}
