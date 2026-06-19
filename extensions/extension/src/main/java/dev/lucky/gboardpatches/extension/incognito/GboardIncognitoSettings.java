package dev.lucky.gboardpatches.extension.incognito;

import android.content.Context;
import android.content.SharedPreferences;
import java.lang.reflect.Method;

import dev.lucky.gboardpatches.extension.settings.GboardPatchesSettingsProvider;

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
        if (prefs == null) return true;
        return prefs.getBoolean(KEY_FORCE_INCOGNITO, true);
    }

    public static void setForceIncognitoEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs != null) {
            prefs.edit().putBoolean(KEY_FORCE_INCOGNITO, enabled).commit();
        }
    }

    public static boolean isAllowClipboardEnabled(Context context) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs == null) return true;
        return prefs.getBoolean(KEY_ALLOW_CLIPBOARD, true);
    }

    public static void setAllowClipboardEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs != null) {
            prefs.edit().putBoolean(KEY_ALLOW_CLIPBOARD, enabled).commit();
        }
    }

    public static boolean isAllowVoiceEnabled(Context context) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs == null) return true;
        return prefs.getBoolean(KEY_ALLOW_VOICE, true);
    }

    public static void setAllowVoiceEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs != null) {
            prefs.edit().putBoolean(KEY_ALLOW_VOICE, enabled).commit();
        }
    }
}
