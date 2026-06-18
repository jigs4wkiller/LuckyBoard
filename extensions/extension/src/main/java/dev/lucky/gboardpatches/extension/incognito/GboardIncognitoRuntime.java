package dev.lucky.gboardpatches.extension.incognito;

import android.content.Context;
import android.content.SharedPreferences;

public final class GboardIncognitoRuntime {
    private static final String PREFS_NAME = "luckyboard_incognito";
    private static final String KEY_FORCE_INCOGNITO = "force_incognito";
    private static final String KEY_ALLOW_CLIPBOARD = "allow_clipboard_incognito";
    private static final String KEY_ALLOW_VOICE = "allow_voice_incognito";

    private static Context sAppContext;

    private GboardIncognitoRuntime() {}

    public static void init(Context context) {
        sAppContext = context.getApplicationContext();
    }

    private static SharedPreferences getPrefs() {
        if (sAppContext == null) return null;
        return sAppContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
