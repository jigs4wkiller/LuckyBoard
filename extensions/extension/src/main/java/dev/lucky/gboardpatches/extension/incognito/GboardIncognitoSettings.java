package dev.lucky.gboardpatches.extension.incognito;

import android.content.Context;
import android.content.SharedPreferences;

public final class GboardIncognitoSettings {
    private static final String PREFS_NAME = "luckyboard_incognito";
    private static final String KEY_FORCE_INCOGNITO = "force_incognito";
    private static final String KEY_ALLOW_CLIPBOARD = "allow_clipboard_incognito";
    private static final String KEY_ALLOW_VOICE = "allow_voice_incognito";

    private GboardIncognitoSettings() {}

    public static boolean isForceIncognitoEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_FORCE_INCOGNITO, true);
    }

    public static void setForceIncognitoEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_FORCE_INCOGNITO, enabled).apply();
    }

    public static boolean isAllowClipboardEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_ALLOW_CLIPBOARD, true);
    }

    public static void setAllowClipboardEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_ALLOW_CLIPBOARD, enabled).apply();
    }

    public static boolean isAllowVoiceEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_ALLOW_VOICE, true);
    }

    public static void setAllowVoiceEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_ALLOW_VOICE, enabled).apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
