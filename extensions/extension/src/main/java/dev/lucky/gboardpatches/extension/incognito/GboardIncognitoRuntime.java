package dev.lucky.gboardpatches.extension.incognito;

import android.content.Context;
import android.content.SharedPreferences;
import java.lang.reflect.Method;

import dev.lucky.gboardpatches.extension.settings.GboardPatchesSettingsProvider;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

public final class GboardIncognitoRuntime {
    private static final String PREFS_NAME = "luckyboard_incognito";
    private static final String KEY_FORCE_INCOGNITO = "force_incognito";
    private static final String KEY_ALLOW_CLIPBOARD = "allow_clipboard_incognito";
    private static final String KEY_ALLOW_VOICE = "allow_voice_incognito";

    private GboardIncognitoRuntime() {}

    private static Context getApplicationContext() {
        // Try settings provider first
        Context providerContext = GboardPatchesSettingsProvider.getStaticContext();
        if (providerContext != null) {
            return providerContext;
        }
        // Fallback to ActivityThread
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Method currentApplication = activityThread.getDeclaredMethod("currentApplication");
            Object app = currentApplication.invoke(null);
            if (app instanceof Context) {
                return ((Context) app).getApplicationContext();
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static boolean readBooleanDirectly(String key, boolean defaultValue) {
        try {
            String[] paths = new String[]{
                "/data/data/pre.lucky.com.google.android.inputmethod.latin/shared_prefs/" + PREFS_NAME + ".xml",
                "/data/data/com.google.android.inputmethod.latin/shared_prefs/" + PREFS_NAME + ".xml"
            };
            for (String path : paths) {
                File file = new File(path);
                if (file.exists()) {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();
                    String xml = sb.toString();
                    java.util.regex.Matcher m = java.util.regex.Pattern
                            .compile("name=\"" + key + "\">([^<]+)<")
                            .matcher(xml);
                    if (m.find()) {
                        return Boolean.parseBoolean(m.group(1));
                    }
                }
            }
        } catch (Throwable ignored) {}
        return defaultValue;
    }

    private static SharedPreferences getPrefs() {
        Context context = getApplicationContext();
        if (context == null) return null;
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean shouldForceIncognito() {
        SharedPreferences prefs = getPrefs();
        if (prefs != null) {
            return prefs.getBoolean(KEY_FORCE_INCOGNITO, true);
        }
        return readBooleanDirectly(KEY_FORCE_INCOGNITO, true);
    }

    public static boolean shouldAllowClipboard() {
        SharedPreferences prefs = getPrefs();
        if (prefs != null) {
            return prefs.getBoolean(KEY_ALLOW_CLIPBOARD, true);
        }
        return readBooleanDirectly(KEY_ALLOW_CLIPBOARD, true);
    }

    public static boolean shouldAllowVoice() {
        SharedPreferences prefs = getPrefs();
        if (prefs != null) {
            return prefs.getBoolean(KEY_ALLOW_VOICE, true);
        }
        return readBooleanDirectly(KEY_ALLOW_VOICE, true);
    }

    public static void updateStaticField() {
        try {
            Class<?> jakClass = Class.forName("jak");
            java.lang.reflect.Field field = jakClass.getDeclaredField("sForceIncognito");
            field.setBoolean(null, shouldForceIncognito());
        } catch (Throwable ignored) {}
    }
}
