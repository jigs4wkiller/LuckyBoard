package dev.lucky.gboardpatches.pngcompanion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Receiver that listens for optimization requests from the LuckyBoard PNG patch.
 * Runs the optimization in background and signals completion.
 *
 * The patch writes a request file with PNG paths, sends this broadcast.
 * This receiver processes them using the pure optimizer (can be improved here with more libs in the companion's own gradle).
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

        File requestFile = new File(requestPath);
        File workDir = requestFile.getParentFile();
        if (workDir == null) workDir = new File("/sdcard/LuckyBoardPngWork");
        final File doneFile = new File(workDir, DONE_FILE);

        // Clear old done
        doneFile.delete();

        // Process in background
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... voids) {
                int processed = 0;
                try {
                    if (!requestFile.exists()) return 0;

                    BufferedReader reader = new BufferedReader(new FileReader(requestFile));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        File png = new File(line.trim());
                        if (png.exists() && png.isFile()) {
                            // Use the pure optimizer logic (copy the basicOptimizePngPure here or call a util)
                            if (optimizePngPure(png)) {
                                processed++;
                            }
                        }
                    }
                    reader.close();

                    // Signal done
                    FileWriter writer = new FileWriter(doneFile);
                    writer.write("processed=" + processed + "\n");
                    writer.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return processed;
            }

            @Override
            protected void onPostExecute(Integer processed) {
                Toast.makeText(context, "LuckyBoard PNG Companion: optimized " + processed + " files", Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    // Copy of the pure basic optimizer (from the patch) - for the companion to use.
    // In real companion, you can enhance this method with more advanced pure or lib-based opt.
    private static boolean optimizePngPure(File file) {
        // Minimal re-compress logic (same as fallback in patch)
        try {
            byte[] orig = java.nio.file.Files.readAllBytes(file.toPath());
            if (orig.length < 8 || !isPngSig(orig)) return false;

            // (For brevity in this skeleton, use the same logic as the patch's basicOptimizePngPure.
            // In practice, copy the full pure function here or share via a library module.)
            // For this example, we simulate a "better" pass by just touching the file or basic.
            // Real: implement the IDAT re-compress here.

            // Placeholder: to demonstrate, we can do nothing or a dummy.
            // To have real effect, the full parser + Deflater from the patch should be copied here.
            // For now, to avoid duplication, assume user copies the logic.

            // Simple: re-write the file (no change) to show the flow.
            // In production companion, put the full pureOptimize here.

            return false; // For skeleton, no change. User can flesh out.
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isPngSig(byte[] b) {
        byte[] sig = new byte[] {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        if (b.length < 8) return false;
        for (int i = 0; i < 8; i++) if (b[i] != sig[i]) return false;
        return true;
    }
}
