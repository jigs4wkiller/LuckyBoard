package dev.lucky.gboardpatches.patches.universal

import app.morphe.patcher.patch.resourcePatch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.concurrent.TimeUnit
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * Universal PNG Optimizer patch - COMPANION MODE (no external libs in patch).
 *
 * Pure-JDK delegation to separate "LuckyBoard PNG Companion" APK.
 * This solves the ant.Task / ClassNotFound + 0 freed problems of previous approaches.
 *
 * Patch only does discovery + copy to public workdir + broadcast + copyback.
 * Real work (and future better algos / pngtastic / filters) lives in the companion.
 *
 * Install companion + grant "all files" (11+) for both Morphe and companion.
 * Fallback gives basically 0KB (expected).
 *
 * Safe for 9-patches (pixels untouched, only IDAT re-compress + safe chunk drop).
 */
internal val universalPngOptimizerPatch = resourcePatch(
    name = "Universal PNG Optimizer",
    description = "Optimizes PNG + 9.png by delegating to LuckyBoard PNG Companion app (separate process, 'andere Weise'). Install companion APK + grant all-files access for real savings (basic fallback in-patch usually 0KB). Pure, no ext libs inside .mpp. UNIVERSAL.",
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

        println("  PngOptimizer (companion delegation mode) working...")
        println("  Target directory: \"${root.absolutePath}\"")
        println("  (Install 'LuckyBoard PNG Companion' APK + enable all-files access for >0KB results)")

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

        println("  Found ${regularPngs.size} regular PNG(s), ${ninePngs.size} 9.png file(s).")

        if (regularPngs.isEmpty() && ninePngs.isEmpty()) {
            println("  Done (nothing to do).")
            return@finalize
        }

        val allPngs = regularPngs + ninePngs

        // Try companion first
        var usedCompanion = false
        if (isCompanionInstalled()) {
            println("  Companion found, delegating optimization...")
            if (delegateToCompanion(allPngs)) {
                usedCompanion = true
                println("  Companion optimization completed.")
            } else {
                println("  Companion delegation failed or timed out, falling back...")
            }
        }

        if (!usedCompanion) {
            // Fallback: basic pure JDK (usually 0kb - this is why companion exists)
            println("  No companion or delegation failed - using weak in-patch fallback (install companion APK for real opt in other process)...")
            var processed = 0
            allPngs.forEach { f ->
                if (basicOptimizePngPure(f)) processed++
            }
            println("  Fallback processed: $processed")
        }

        val sAfter = calculateTotalSize(root)
        val freed = (sBefore - sAfter) / 1024

        println("  Done!")
        println("  Results:")
        println("      Start time: $startAt        End time: ${java.time.LocalTime.now()}")
        println("      Files processed: ${allPngs.size}")
        println("           Freed: $freed Kb")
        if (usedCompanion) {
            println("  (Via companion process - extend optimizePngPure in companion for best results)")
        } else {
            println("  (Basic in-patch fallback - 0KB is normal. Install + grant perms to LuckyBoard PNG Companion APK)")
        }
    }
}

private fun calculateTotalSize(dir: File): Long {
    return dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
}

// --- Companion integration ---

private const val COMPANION_PACKAGE = "dev.lucky.gboardpatches.pngcompanion"
private const val COMPANION_ACTION = "dev.lucky.gboardpatches.pngcompanion.OPTIMIZE_PNGS"
private const val WORK_DIR = "/sdcard/LuckyBoardPngWork"  // Shared, user-writable dir

private fun getAppContext(): Any? {
    // Obtain application context via reflection. Works for injected patch code running in Morphe process.
    // Returns the Context as Any to avoid any compile-time android.* dependency.
    return try {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val currentApplicationMethod = activityThreadClass.getMethod("currentApplication")
        currentApplicationMethod.invoke(null)
    } catch (_: Exception) {
        null
    }
}

private fun isCompanionInstalled(): Boolean {
    val ctx = getAppContext() ?: return false
    return try {
        val pm = ctx.javaClass.getMethod("getPackageManager").invoke(ctx)
        pm.javaClass.getMethod("getPackageInfo", String::class.java, Int::class.javaPrimitiveType)
            .invoke(pm, COMPANION_PACKAGE, 0)
        true
    } catch (_: Exception) {
        false
    }
}

private fun copyFile(src: File, dst: File) {
    FileInputStream(src).use { input ->
        FileOutputStream(dst).use { output ->
            input.copyTo(output)
        }
    }
}

private fun delegateToCompanion(pngFiles: List<File>): Boolean {
    try {
        val workDir = File(WORK_DIR)
        if (!workDir.exists() && !workDir.mkdirs()) {
            println("  Cannot create work dir $WORK_DIR (grant storage/all-files access?)")
            return false
        }

        val mapFile = File(workDir, "map.txt")
        val doneFile = File(workDir, "done.txt")

        // Clean previous
        doneFile.delete()
        mapFile.delete()
        workDir.listFiles()?.filter { it.name != "map.txt" && it.name != "done.txt" }?.forEach { it.delete() }

        // Copy each PNG to public work dir (companion can read/write), write origPath|workCopyPath map
        OutputStreamWriter(FileOutputStream(mapFile), Charsets.UTF_8).use { writer ->
            pngFiles.forEach { orig ->
                val safe = orig.absolutePath.replace("/", "_").replace(":", "_").replace("\\", "_")
                val workCopy = File(workDir, safe)
                try {
                    copyFile(orig, workCopy)
                    writer.write(orig.absolutePath + "|" + workCopy.absolutePath + "\n")
                } catch (e: Exception) {
                    println("  Skip copy ${orig.name}: ${e.message}")
                }
            }
        }

        // Send broadcast using obtained context via full reflection (no android import, works at runtime in Morphe)
        val ctx = getAppContext()
        var broadcastSent = false
        if (ctx != null) {
            try {
                val intentClass = Class.forName("android.content.Intent")
                val intent = intentClass.getConstructor(String::class.java).newInstance(COMPANION_ACTION)
                val putExtraM = intentClass.getMethod("putExtra", String::class.java, String::class.java)
                putExtraM.invoke(intent, "request_file", mapFile.absolutePath)
                val addFlagsM = intentClass.getMethod("addFlags", Int::class.javaPrimitiveType)
                addFlagsM.invoke(intent, 0x00000004) // FLAG_INCLUDE_STOPPED_PACKAGES (old but useful)

                val sendM = ctx.javaClass.getMethod("sendBroadcast", intentClass)
                sendM.invoke(ctx, intent)
                broadcastSent = true
                println("  Broadcast sent to companion for offload opt (different process).")
            } catch (e: Exception) {
                println("  Broadcast reflection failed: ${e.message}. Request file still written.")
            }
        }
        if (!broadcastSent) {
            println("  Context unavailable or send failed; companion may still pick up via manual trigger in its UI (or auto if already listening dir).")
        }

        println("  Waiting for companion (copies in $mapFile, up to 60s)...")

        // Poll
        val start = System.currentTimeMillis()
        val timeout = 60_000L
        var responded = false
        while (System.currentTimeMillis() - start < timeout) {
            if (doneFile.exists()) {
                responded = true
                break
            }
            Thread.sleep(800)
        }

        if (!responded) {
            println("  Companion did not respond in time.")
            cleanupWork(workDir, mapFile, doneFile)
            return false
        }

        // Copy optimized (smaller) work copies back to original private locations
        var copiedBack = 0
        mapFile.forEachLine { line ->
            if (line.isBlank()) return@forEachLine
            val parts = line.trim().split("|", limit = 2)
            if (parts.size == 2) {
                val origF = File(parts[0])
                val workF = File(parts[1])
                if (workF.exists() && workF.isFile && origF.exists()) {
                    if (workF.length() <= origF.length()) {
                        try {
                            copyFile(workF, origF)
                            copiedBack++
                        } catch (_: Exception) {}
                    }
                    workF.delete()
                }
            }
        }

        println("  Companion finished, copied back $copiedBack optimized PNG(s) from work dir.")
        cleanupWork(workDir, mapFile, doneFile)
        return copiedBack > 0 || true // success even if 0 smaller (companion ran)
    } catch (e: Exception) {
        println("  Delegation error: ${e.message}")
        return false
    }
}

private fun cleanupWork(workDir: File, mapFile: File, doneFile: File) {
    try {
        mapFile.delete()
        doneFile.delete()
        workDir.listFiles()?.filter { it.isFile && (it.name.endsWith(".png") || it.name.contains("_")) }?.forEach { it.delete() }
    } catch (_: Exception) {}
}

// --- Fallback basic pure JDK (re-compress + strip non-critical chunks for small gains) ---

private fun basicOptimizePngPure(file: File): Boolean {
    val orig = file.readBytes()
    if (orig.size < 8 || !isPngSig(orig)) return false

    try {
        val chunks = parseChunks(orig)
        val idatOut = ByteArrayOutputStream()
        chunks.filter { it.type == "IDAT" }.forEach { idatOut.write(it.data) }
        if (idatOut.size() == 0) return false

        val comp = idatOut.toByteArray()

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

        // Strip non-critical ancillary chunks (text, time etc.) - companion can do even more
        val filtered = filterEssentialChunks(newChunks)

        // Only accept if we actually made it smaller (strip + re-compress can win even if IDAT same size)
        val candidate = buildPng(filtered)
        if (candidate.size < orig.size) {
            file.writeBytes(candidate)
            return true
        }
    } catch (_: Exception) {}
    return false
}

private fun filterEssentialChunks(chunks: List<Chunk>): List<Chunk> {
    // Drop bloat that does not affect rendering (safe for Gboard drawables and 9-patches)
    val dropTypes = setOf("tIME", "tEXt", "iTXt", "zTXt", "hIST", "sTER", "pHYs")
    return chunks.filter { it.type == "IDAT" || it.type == "IHDR" || it.type == "PLTE" || it.type == "IEND" || it.type == "tRNS" || !dropTypes.contains(it.type) }
}

// --- Shared pure helpers (used by fallback and can be used by companion) ---

private data class Chunk(val type: String, val data: ByteArray)

private fun isPngSig(b: ByteArray): Boolean {
    val sig = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
    return b.size >= 8 && sig.indices.all { b[it] == sig[it] }
}

private fun parseChunks(b: ByteArray): List<Chunk> {
    val out = mutableListOf<Chunk>()
    var p = 8
    while (p + 12 <= b.size) {
        val len = ((b[p].toInt() and 0xff) shl 24) or ((b[p+1].toInt() and 0xff) shl 16) or ((b[p+2].toInt() and 0xff) shl 8) or (b[p+3].toInt() and 0xff)
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
        out.write((l ushr 24) and 0xff); out.write((l ushr 16) and 0xff); out.write((l ushr 8) and 0xff); out.write(l and 0xff)
        out.write(tb)
        out.write(c.data)
        crc.reset(); crc.update(tb); crc.update(c.data)
        val cv = crc.value.toInt()
        out.write((cv ushr 24) and 0xff); out.write((cv ushr 16) and 0xff); out.write((cv ushr 8) and 0xff); out.write(cv and 0xff)
    }
    return out.toByteArray()
}
