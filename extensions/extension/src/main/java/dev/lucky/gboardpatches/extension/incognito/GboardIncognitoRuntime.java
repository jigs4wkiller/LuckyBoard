package dev.lucky.gboardpatches.extension.incognito;

import android.content.Context;
import android.content.SharedPreferences;
import java.lang.reflect.Method;

public final class GboardIncognitoRuntime {
    private static final String PREFS_NAME = "luckyboard_incognito";
    private static final String KEY_FORCE_INCOGNITO = "force_incognito";
    private static final String KEY_ALLOW_CLIPBOARD = "allow_clipboard_incognito";
    private static final String KEY_ALLOW_VOICE = "allow_voice_incognito";

    private GboardIncognitoRuntime() {}

    private static Context getApplicationContext() {
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

    private static SharedPreferences getPrefs() {
        Context context = getApplicationContext();
        if (context == null) return null;
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean shouldForceIncognito() {
        SharedPreferences prefs = getPrefs();
        if (prefs == null) return true;
        return prefs.getBoolean(KEY_FORCE_INCOGNITO, true);
    }

    public static boolean shouldAllowClipboard() {
        SharedPreferences prefs = getPrefs();
        if (prefs == null) return true;
        return prefs.getBoolean(KEY_ALLOW_CLIPBOARD, true);
    }

    public static boolean shouldAllowVoice() {
        SharedPreferences prefs = getPrefs();
        if (prefs == null) return true;
        return prefs.getBoolean(KEY_ALLOW_VOICE, true);
    }
}
