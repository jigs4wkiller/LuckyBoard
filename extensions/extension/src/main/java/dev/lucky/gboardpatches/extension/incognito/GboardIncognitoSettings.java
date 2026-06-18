package dev.lucky.gboardpatches.extension.incognito;

import android.content.Context;
import android.content.SharedPreferences;

public final class GboardIncognitoSettings {
    private static final String PREFS_NAME = "luckyboard_incognito";
    private static final String KEY_ENABLED = "incognito_enabled";

    private GboardIncognitoSettings() {}

    public static boolean isEnabled(Context context) {
        return getPrefs(context).getBoolean(KEY_ENABLED, true);
    }

    public static void setEnabled(Context context, boolean enabled) {
        getPrefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
