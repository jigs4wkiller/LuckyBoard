package dev.lucky.gboardpatches.extension.incognito;

import android.content.Context;
import android.content.SharedPreferences;

public final class GboardIncognitoRuntime {
    private static final String PREFS_NAME = "luckyboard_incognito";
    private static final String KEY_FORCE_INCOGNITO = "force_incognito";
    private static final String KEY_ALLOW_CLIPBOARD = "allow_clipboard_incognito";
    private static final String KEY_ALLOW_VOICE = "allow_voice_incognito";

    private GboardIncognitoRuntime() {}

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean shouldForceIncognito(Context context) {
        return getPrefs(context).getBoolean(KEY_FORCE_INCOGNITO, true);
    }

    public static boolean shouldAllowClipboard(Context context) {
        return getPrefs(context).getBoolean(KEY_ALLOW_CLIPBOARD, true);
    }

    public static boolean shouldAllowVoice(Context context) {
        return getPrefs(context).getBoolean(KEY_ALLOW_VOICE, true);
    }
}
