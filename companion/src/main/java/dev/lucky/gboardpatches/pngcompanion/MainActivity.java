package dev.lucky.gboardpatches.pngcompanion;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Simple launcher for LuckyBoard PNG Companion.
 * Shows status + manual trigger for the request from Morphe patch.
 * On Android 11+ user must grant "All files access" in system settings for /sdcard workdir.
 */
public class MainActivity extends Activity {

    private static final int REQ_STORAGE = 42;
    private TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 48);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("LuckyBoard PNG Companion");
        title.setTextSize(22);
        title.setPadding(0, 0, 0, 24);
        layout.addView(title);

        TextView info = new TextView(this);
        info.setText("This app helps the Universal PNG Optimizer patch (in LuckyBoard patches for Morphe) get better PNG savings by running optimization in a separate process.\n\n" +
                "1. Grant storage permission / All files access (Android 11+).\n" +
                "2. Install + enable the PNG Optimizer patch in Morphe.\n" +
                "3. When Morphe applies patches, this will auto-receive and process (toast will show).\n\n" +
                "Work dir: /sdcard/LuckyBoardPngWork (shared via broadcast request map)\n\n" +
                "Fallback in-patch gives ~0KB. Companion enables different/better methods.");
        info.setTextSize(14);
        layout.addView(info);

        statusView = new TextView(this);
        statusView.setPadding(0, 32, 0, 16);
        statusView.setText("Status: ready");
        layout.addView(statusView);

        Button permBtn = new Button(this);
        permBtn.setText("Request storage permission");
        permBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPerms();
            }
        });
        layout.addView(permBtn);

        Button manualBtn = new Button(this);
        manualBtn.setText("Manually process pending request");
        manualBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualProcess();
            }
        });
        layout.addView(manualBtn);

        Button clearBtn = new Button(this);
        clearBtn.setText("Clear work dir");
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearWork();
            }
        });
        layout.addView(clearBtn);

        setContentView(layout);

        updateStatus();
    }

    private void requestPerms() {
        if (Build.VERSION.SDK_INT >= 30) {
            // Android 11+: direct "all files" is special permission, guide user
            Toast.makeText(this, "On Android 11+ go to system Settings > Apps > LuckyBoard PNG Companion > Permissions > Allow access to all files", Toast.LENGTH_LONG).show();
            try {
                Intent i = new Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(i);
            } catch (Exception e) {
                Toast.makeText(this, "Open settings manually for 'All files access'", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQ_STORAGE);
            } else {
                Toast.makeText(this, "Already granted", Toast.LENGTH_SHORT).show();
            }
        }
        updateStatus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_STORAGE) {
            boolean ok = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            Toast.makeText(this, ok ? "Granted" : "Denied - use All files on 11+", Toast.LENGTH_SHORT).show();
            updateStatus();
        }
    }

    private void updateStatus() {
        File work = new File("/sdcard/LuckyBoardPngWork");
        String s = "Work dir: " + (work.exists() ? "exists" : "not created yet");
        if (work.exists()) {
            File[] fs = work.listFiles();
            s += " (" + (fs != null ? fs.length : 0) + " files)";
        }
        if (Build.VERSION.SDK_INT >= 30) {
            s += "\nNote: Android 11+ needs 'All files access' enabled for this app AND for Morphe Manager.";
        }
        statusView.setText(s);
    }

    private void manualProcess() {
        File workDir = new File("/sdcard/LuckyBoardPngWork");
        File map = new File(workDir, "map.txt");
        if (!map.exists()) {
            // fallback old name
            map = new File(workDir, "request.txt");
        }
        if (!map.exists()) {
            Toast.makeText(this, "No pending map/request.txt found in work dir", Toast.LENGTH_SHORT).show();
            return;
        }
        // Reuse the receiver logic by constructing a fake intent or direct call
        // For simplicity here: call a static util if we extract, or re-do minimal processing
        int count = processMapOrRequest(map, workDir);
        Toast.makeText(this, "Manual processed: " + count, Toast.LENGTH_SHORT).show();
        // write done so if patch is waiting it can proceed (best effort)
        try {
            File done = new File(workDir, "done.txt");
            FileWriter w = new FileWriter(done);
            w.write("processed=" + count + " (manual)\n");
            w.close();
        } catch (Exception ignored) {}
        updateStatus();
    }

    private int processMapOrRequest(File mapFile, File workDir) {
        int processed = 0;
        try {
            BufferedReader r = new BufferedReader(new FileReader(mapFile));
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String workPath;
                if (line.contains("|")) {
                    workPath = line.split("\\|", 2)[1];
                } else {
                    workPath = line; // old direct request style (may fail access)
                }
                File png = new File(workPath);
                if (png.exists() && png.isFile() && PngOptimizeReceiver.optimizePngPureStatic(png)) {
                    processed++;
                }
            }
            r.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // also try to signal
        try {
            File done = new File(workDir, "done.txt");
            FileWriter w = new FileWriter(done, true);
            w.write("manual=" + processed + "\n");
            w.close();
        } catch (Exception ignored) {}
        return processed;
    }

    private void clearWork() {
        File work = new File("/sdcard/LuckyBoardPngWork");
        if (work.exists()) {
            File[] files = work.listFiles();
            if (files != null) for (File f : files) f.delete();
            work.delete();
        }
        Toast.makeText(this, "Work dir cleared", Toast.LENGTH_SHORT).show();
        updateStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
}