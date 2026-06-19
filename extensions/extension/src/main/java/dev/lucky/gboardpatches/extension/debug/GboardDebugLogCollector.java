package dev.lucky.gboardpatches.extension.debug;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public final class GboardDebugLogCollector {
    private static final String TAG = "GboardPatches";

    private GboardDebugLogCollector() {}

    public static String collectLogs() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== LuckyBoard Debug Log ===\n");
        sb.append("Timestamp: ").append(new java.util.Date().toString()).append("\n\n");

        try {
            Process process = Runtime.getRuntime().exec("logcat -d -v time");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("GboardPatches") || line.contains("Keyboard") || line.contains("Incognito")
                        || line.contains("clipboard") || line.contains("Gboard")) {
                    sb.append(line).append("\n");
                }
            }
            reader.close();
        } catch (Exception e) {
            sb.append("Error reading logcat: ").append(e.getMessage()).append("\n");
        }

        sb.append("\n=== Package Info ===\n");
        try {
            sb.append("Package: ").append("pre.lucky.com.google.android.inputmethod.latin").append("\n");
        } catch (Exception e) {
            sb.append("Error: ").append(e.getMessage()).append("\n");
        }

        return sb.toString();
    }

    public static void copyLogsToClipboard(Context context) {
        String logs = collectLogs();
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("LuckyBoard Debug Log", logs);
            clipboard.setPrimaryClip(clip);
            Log.i(TAG, "Debug log copied to clipboard (" + logs.length() + " chars)");
        }
    }
}
