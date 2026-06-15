package dev.jason.gboardpatches.extension.settings;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public final class GboardPatchesFeatureAvailability {
    public static final String FEATURE_CLIPBOARD_ENHANCEMENTS =
            "dev.jason.gboardpatches.feature.clipboard_enhancements";
    public static final String FEATURE_WEB_CLIPBOARD =
            "dev.jason.gboardpatches.feature.web_clipboard";
    public static final String FEATURE_CLIPBOARD_ENTITY_EXTRACTION =
            "dev.jason.gboardpatches.feature.clipboard_entity_extraction";
    public static final String FEATURE_CLIPBOARD_ITEM_EDIT =
            "dev.jason.gboardpatches.feature.clipboard_item_edit";
    public static final String FEATURE_GRAMMAR_CHECKER =
            "dev.jason.gboardpatches.feature.grammar_checker";
    public static final String FEATURE_INLINE_SUGGESTIONS =
            "dev.jason.gboardpatches.feature.inline_suggestions";
    public static final String FEATURE_KEY_SHAPE_SELECTION =
            "dev.jason.gboardpatches.feature.key_shape_selection";
    public static final String FEATURE_CUSTOM_SYMBOLS =
            "dev.jason.gboardpatches.feature.custom_symbols";
    public static final String FEATURE_SETTINGS_HOMEPAGE =
            "dev.jason.gboardpatches.feature.settings_homepage";
    public static final String FEATURE_SYMBOL_FOOTER_ORDER =
            "dev.jason.gboardpatches.feature.symbol_footer_order";
    public static final String FEATURE_LATIN_GLOBE_KEY_IGNORE_INTERVAL =
            "dev.jason.gboardpatches.feature.latin_globe_key_ignore_interval";
    public static final String FEATURE_FEATURE_FLAGS_UI =
            "dev.jason.gboardpatches.feature.feature_flags_ui";
    public static final String FEATURE_FLAG_SUPPORT_ACCESSORY_KEYBOARD =
            "dev.jason.gboardpatches.feature.flag_support_accessory_keyboard";
    public static final String FEATURE_FLAG_ENABLE_VOICE_WIDGET =
            "dev.jason.gboardpatches.feature.flag_enable_voice_widget";
    public static final String FEATURE_FLAG_ENABLE_NEW_LANGUAGE_SEARCH_BAR =
            "dev.jason.gboardpatches.feature.flag_enable_new_language_search_bar";
    public static final String FEATURE_FLAG_ENABLE_SETTINGS_TWO_PANE =
            "dev.jason.gboardpatches.feature.flag_enable_settings_two_pane";
    public static final String FEATURE_FLAG_ENABLE_OCR =
            "dev.jason.gboardpatches.feature.flag_enable_ocr";
    public static final String FEATURE_FLAG_ENABLE_DICTATION_REDESIGN =
            "dev.jason.gboardpatches.feature.flag_enable_dictation_redesign";
    public static final String FEATURE_FLAG_SHOW_COLLAPSE_BUTTON =
            "dev.jason.gboardpatches.feature.flag_show_collapse_button";
    public static final String FEATURE_KEY_SHAPE_CUSTOMIZATION =
            "dev.jason.gboardpatches.feature.key_shape_customization";

    // Additional good flags discovered in decompiled Gboard (pdk registration + other smali)
    // from Rboard/GboardThemes community patterns and useful experimental features.
    public static final String FEATURE_FLAG_ENABLE_ADJUST_DEFAULT_KEYBOARD_HEIGHT =
            "dev.jason.gboardpatches.feature.flag_enable_adjust_default_keyboard_height";
    public static final String FEATURE_FLAG_SILK_THEME =
            "dev.jason.gboardpatches.feature.flag_silk_theme";
    public static final String FEATURE_FLAG_MATERIAL3_THEME =
            "dev.jason.gboardpatches.feature.flag_material3_theme";
    public static final String FEATURE_FLAG_SILK_POPUP =
            "dev.jason.gboardpatches.feature.flag_silk_popup";
    public static final String FEATURE_FLAG_ENABLE_AI_CORE_SMART_REPLY =
            "dev.jason.gboardpatches.feature.flag_enable_ai_core_smart_reply";
    public static final String FEATURE_FLAG_KEYBOARD_REDESIGN_GOOGLE_SANS =
            "dev.jason.gboardpatches.feature.flag_keyboard_redesign_google_sans";
    public static final String FEATURE_FLAG_BRIGHT_KEY_ON_DYNAMIC_COLOR_DARK_THEME =
            "dev.jason.gboardpatches.feature.flag_bright_key_on_dynamic_color_dark_theme";

    private static final String TAG = "GboardPatches";

    private GboardPatchesFeatureAvailability() {
    }

    public static boolean hasFeature(Context context, String featureKey) {
        if (context == null || featureKey == null || featureKey.isEmpty()) {
            return false;
        }
        return hasAnyFeature(context, featureKey);
    }

    public static boolean hasAnyFeature(Context context, String... featureKeys) {
        if (context == null || featureKeys == null || featureKeys.length == 0) {
            return false;
        }
        for (String featureKey : featureKeys) {
            if (featureKey == null || featureKey.isEmpty()) {
                return false;
            }
        }

        Context applicationContext = context.getApplicationContext();
        Context lookupContext = applicationContext != null ? applicationContext : context;
        try {
            PackageManager packageManager = lookupContext.getPackageManager();
            if (packageManager == null) {
                return false;
            }
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(
                    lookupContext.getPackageName(),
                    PackageManager.GET_META_DATA);
            Bundle metaData = applicationInfo.metaData;
            if (metaData == null) {
                return false;
            }
            for (String featureKey : featureKeys) {
                if (metaData.getBoolean(featureKey, false)) {
                    return true;
                }
            }
            return false;
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to resolve feature marker: " + describeFeatures(featureKeys),
                    throwable);
            return false;
        }
    }

    private static String describeFeatures(String[] featureKeys) {
        if (featureKeys.length == 1) {
            return featureKeys[0];
        }

        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < featureKeys.length; index++) {
            if (index > 0) {
                builder.append(", ");
            }
            builder.append(featureKeys[index]);
        }
        return builder.toString();
    }
}
