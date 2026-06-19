package dev.lucky.gboardpatches.extension.incognito;

import android.content.Context;
import android.content.SharedPreferences;
import java.lang.reflect.Method;

import dev.lucky.gboardpatches.extension.settings.GboardPatchesSettingsProvider;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

public final class GboardIncognitoSettings {
    private static final String PREFS_NAME = "luckyboard_incognito";
    private static final String KEY_FORCE_INCOGNITO = "force_incognito";
    private static final String KEY_ALLOW_CLIPBOARD = "allow_clipboard_incognito";
    private static final String KEY_ALLOW_VOICE = "allow_voice_incognito";

    private GboardIncognitoSettings() {}

    private static Context getApplicationContext() {
        Context providerContext = GboardPatchesSettingsProvider.getStaticContext();
        if (providerContext != null) {
            return providerContext;
        }
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
                java.io.File file = new java.io.File(path);
                if (file.exists()) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
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

    private static void writeBooleanDirectly(String key, boolean value) {
        try {
            String path = "/data/data/pre.lucky.com.google.android.inputmethod.latin/shared_prefs/" + PREFS_NAME + ".xml";
            java.io.File file = new java.io.File(path);
            if (!file.exists()) {
                // Try to create the file with default content
                java.io.FileWriter writer = new java.io.FileWriter(file);
                writer.write("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?><map></map>");
                writer.close();
            }
            // Read existing content
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            String xml = sb.toString();
            // Replace or add the value
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("name=\"" + key + "\">[^<]*<");
            java.util.regex.Matcher matcher = pattern.matcher(xml);
            String newValue = " name=\"" + key + "\">" + value + "<";
            if (matcher.find()) {
                xml = matcher.replaceAll(newValue);
            } else {
                xml = xml.replace("</map>", newValue + "</map>");
            }
            java.io.FileWriter writer = new java.io.FileWriter(file);
            writer.write(xml);
            writer.close();
        } catch (Throwable ignored) {}
    }

    private static SharedPreferences getPrefs(Context fallbackContext) {
        Context context = getApplicationContext();
        if (context == null) {
            context = fallbackContext;
        }
        if (context == null) return null;
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isForceIncognitoEnabled(Context context) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs != null) {
            return prefs.getBoolean(KEY_FORCE_INCOGNITO, true);
        }
        return readBooleanDirectly(KEY_FORCE_INCOGNITO, true);
    }

    public static void setForceIncognitoEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs != null) {
            prefs.edit().putBoolean(KEY_FORCE_INCOGNITO, enabled).commit();
        } else {
            writeBooleanDirectly(KEY_FORCE_INCOGNITO, enabled);
        }
    }

    public static boolean isAllowClipboardEnabled(Context context) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs != null) {
            return prefs.getBoolean(KEY_ALLOW_CLIPBOARD, true);
        }
        return readBooleanDirectly(KEY_ALLOW_CLIPBOARD, true);
    }

    public static void setAllowClipboardEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs != null) {
            prefs.edit().putBoolean(KEY_ALLOW_CLIPBOARD, enabled).commit();
        } else {
            writeBooleanDirectly(KEY_ALLOW_CLIPBOARD, enabled);
        }
    }

    public static boolean isAllowVoiceEnabled(Context context) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs != null) {
            return prefs.getBoolean(KEY_ALLOW_VOICE, true);
        }
        return readBooleanDirectly(KEY_ALLOW_VOICE, true);
    }

    public static void setAllowVoiceEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs != null) {
            prefs.edit().putBoolean(KEY_ALLOW_VOICE, enabled).commit();
        } else {
            writeBooleanDirectly(KEY_ALLOW_VOICE, enabled);
        }
    }
}
