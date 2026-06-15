package dev.lucky.gboardpatches.pngcompanion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Receiver that listens for optimization requests from the LuckyBoard / Morphe PNG patch.
 * Optimization runs in this separate app process ("andere weise") so we can use
 * better methods, add libs (e.g. pngtastic), or future native tools without
 * breaking the patches .mpp (no ant.Task, no javax.imageio, pure or extended here).
 *
 * Flow:
 * - Patch (via reflection context) sends broadcast + writes map of privateOrig|publicWorkCopy
 * - We optimize the work copies in place (public /sdcard/LuckyBoardPngWork)
 * - Write done.txt
 * - Patch polls, copies successful smaller results back, cleans up.
 *
 * Install this companion APK + grant All-files/storage access (esp. Android 11+).
 */
public class PngOptimizeReceiver extends BroadcastReceiver {

    private static final String ACTION = "dev.lucky.gboardpatches.pngcompanion.OPTIMIZE_PNGS";
    private static final String REQUEST_EXTRA = "request_file";
    private static final String DONE_FILE = "done.txt";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!ACTION.equals(intent.getAction())) {
            return;
        }

        String requestPath = intent.getStringExtra(REQUEST_EXTRA);
        if (requestPath == null) {
            return;
        }

        final File requestFile = new File(requestPath);
        File workDir = requestFile.getParentFile();
        if (workDir == null) workDir = new File("/sdcard/LuckyBoardPngWork");
        final File workDirF = workDir;
        final File doneFile = new File(workDirF, DONE_FILE);

        doneFile.delete();

        // Run off main thread (modern, no deprecated AsyncTask)
        new Thread(new Runnable() {
            @Override
            public void run() {
                int processed = 0;
                try {
                    if (!requestFile.exists()) return;

                    BufferedReader reader = new BufferedReader(new FileReader(requestFile));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty()) continue;

                        String workPath;
                        if (line.contains("|")) {
                            // new map format: origPath|workCopyPath  -- we only need work copy
                            String[] p = line.split("\\|", 2);
                            workPath = p.length > 1 ? p[1] : line;
                        } else {
                            workPath = line; // legacy direct
                        }

                        File png = new File(workPath);
                        if (png.exists() && png.isFile()) {
                            if (optimizePngPure(png)) {
                                processed++;
                            }
                        }
                    }
                    reader.close();

                    FileWriter writer = new FileWriter(doneFile);
                    writer.write("processed=" + processed + "\n");
                    writer.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        FileWriter w = new FileWriter(doneFile);
                        w.write("error=1\n");
                        w.close();
                    } catch (Exception ignored) {}
                }

                final int finalProcessed = processed;
                // Toast must be on main thread
                if (context != null) {
                    android.os.Handler handler = new android.os.Handler(context.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "LuckyBoard PNG Companion: optimized " + finalProcessed + " file(s)", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    // --- Real pure optimizer port (same as patch fallback + chunk strip) ---
    // This is "different way": lives in companion APK, can be extended independently
    // (add pngtastic dep in this app's build.gradle for much better results).
    public static boolean optimizePngPure(File file) {
        try {
            byte[] orig = readAllBytes(file);
            if (orig.length < 8 || !isPngSig(orig)) return false;

            List<Chunk> chunks = parseChunks(orig);
            ByteArrayOutputStream idatOut = new ByteArrayOutputStream();
            for (Chunk c : chunks) {
                if ("IDAT".equals(c.type)) idatOut.write(c.data);
            }
            if (idatOut.size() == 0) return false;

            byte[] comp = idatOut.toByteArray();

            Inflater inf = new Inflater();
            inf.setInput(comp);
            ByteArrayOutputStream raw = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            while (!inf.finished()) {
                int n = inf.inflate(buf);
                if (n > 0) raw.write(buf, 0, n);
            }
            inf.end();
            byte[] rawData = raw.toByteArray();
            if (rawData.length == 0) return false;

            Deflater def = new Deflater(Deflater.BEST_COMPRESSION, true);
            def.setInput(rawData);
            def.finish();
            ByteArrayOutputStream newComp = new ByteArrayOutputStream();
            while (!def.finished()) {
                int n = def.deflate(buf);
                if (n > 0) newComp.write(buf, 0, n);
            }
            def.end();
            byte[] newIdat = newComp.toByteArray();

            List<Chunk> newChunks = new ArrayList<Chunk>();
            boolean replaced = false;
            for (Chunk c : chunks) {
                if ("IDAT".equals(c.type)) {
                    if (!replaced) {
                        newChunks.add(new Chunk("IDAT", newIdat));
                        replaced = true;
                    }
                } else {
                    newChunks.add(c);
                }
            }
            if (!replaced) newChunks.add(new Chunk("IDAT", newIdat));

            // Companion advantage: we can also drop more here or try extra passes
            List<Chunk> filtered = filterEssentialChunks(newChunks);

            byte[] candidate = (newIdat.length < comp.length) ? buildPng(filtered) : buildPng(newChunks);
            if (candidate.length < orig.length) {
                writeAllBytes(file, candidate);
                return true;
            }
        } catch (Exception e) {
            // silent per file
        }
        return false;
    }

    // Exposed static for MainActivity manual trigger (no instance needed)
    public static boolean optimizePngPureStatic(File file) {
        return optimizePngPure(file);
    }

    private static byte[] readAllBytes(File f) throws IOException {
        // Support old Android without Files.readAllBytes
        FileInputStream fis = new FileInputStream(f);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int r;
        while ((r = fis.read(buffer)) != -1) {
            bos.write(buffer, 0, r);
        }
        fis.close();
        return bos.toByteArray();
    }

    private static void writeAllBytes(File f, byte[] data) throws IOException {
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(data);
        fos.close();
    }

    private static boolean isPngSig(byte[] b) {
        byte[] sig = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        if (b.length < 8) return false;
        for (int i = 0; i < 8; i++) if (b[i] != sig[i]) return false;
        return true;
    }

    private static class Chunk {
        final String type;
        final byte[] data;
        Chunk(String type, byte[] data) { this.type = type; this.data = data; }
    }

    private static List<Chunk> parseChunks(byte[] b) {
        List<Chunk> out = new ArrayList<Chunk>();
        int p = 8;
        while (p + 12 <= b.length) {
            int len = ((b[p] & 0xff) << 24) | ((b[p+1] & 0xff) << 16) | ((b[p+2] & 0xff) << 8) | (b[p+3] & 0xff);
            if (p + 12 + len > b.length) break;
            String typ = new String(b, p + 4, 4, java.nio.charset.StandardCharsets.US_ASCII);
            byte[] dat = (len == 0) ? new byte[0] : java.util.Arrays.copyOfRange(b, p + 8, p + 8 + len);
            out.add(new Chunk(typ, dat));
            p += 12 + len;
            if ("IEND".equals(typ)) break;
        }
        return out;
    }

    private static byte[] buildPng(List<Chunk> chunks) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(new byte[]{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
            CRC32 crc = new CRC32();
            for (Chunk c : chunks) {
                byte[] tb = c.type.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
                int l = c.data.length;
                out.write((l >>> 24) & 0xff); out.write((l >>> 16) & 0xff); out.write((l >>> 8) & 0xff); out.write(l & 0xff);
                out.write(tb);
                out.write(c.data);
                crc.reset(); crc.update(tb); crc.update(c.data);
                long cv = crc.getValue();
                out.write((int)((cv >>> 24) & 0xff)); out.write((int)((cv >>> 16) & 0xff));
                out.write((int)((cv >>> 8) & 0xff)); out.write((int)(cv & 0xff));
            }
        } catch (IOException ignored) {}
        return out.toByteArray();
    }

    private static List<Chunk> filterEssentialChunks(List<Chunk> chunks) {
        Set<String> drop = new HashSet<String>();
        drop.add("tIME"); drop.add("tEXt"); drop.add("iTXt"); drop.add("zTXt");
        drop.add("hIST"); drop.add("sTER"); drop.add("pHYs");
        List<Chunk> res = new ArrayList<Chunk>();
        for (Chunk c : chunks) {
            if ("IDAT".equals(c.type) || "IHDR".equals(c.type) || "PLTE".equals(c.type) ||
                "IEND".equals(c.type) || "tRNS".equals(c.type) || !drop.contains(c.type)) {
                res.add(c);
            }
        }
        return res;
    }
}