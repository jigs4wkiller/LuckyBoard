package dev.lucky.gboardpatches.extension.settings;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public final class GboardPatchesFeatureAvailability {
    public static final String FEATURE_CLIPBOARD_ENHANCEMENTS =
            "dev.lucky.gboardpatches.feature.clipboard_enhancements";
    public static final String FEATURE_WEB_CLIPBOARD =
            "dev.lucky.gboardpatches.feature.web_clipboard";
    public static final String FEATURE_CLIPBOARD_ENTITY_EXTRACTION =
            "dev.lucky.gboardpatches.feature.clipboard_entity_extraction";
    public static final String FEATURE_INCognito =
            "dev.lucky.gboardpatches.feature.incognito_enhancements";
    public static final String FEATURE_DEBUG_PATCHES =
            "dev.lucky.gboardpatches.feature.debug_patches";
    public static final String FEATURE_GRAMMAR_CHECKER =
            "dev.lucky.gboardpatches.feature.grammar_checker";
    public static final String FEATURE_INLINE_SUGGESTIONS =
            "dev.lucky.gboardpatches.feature.inline_suggestions";
    public static final String FEATURE_KEY_SHAPE_SELECTION =
            "dev.lucky.gboardpatches.feature.key_shape_selection";
    public static final String FEATURE_CUSTOM_SYMBOLS =
            "dev.lucky.gboardpatches.feature.custom_symbols";
    public static final String FEATURE_SETTINGS_HOMEPAGE =
            "dev.lucky.gboardpatches.feature.settings_homepage";
    public static final String FEATURE_SYMBOL_FOOTER_ORDER =
            "dev.lucky.gboardpatches.feature.symbol_footer_order";
    public static final String FEATURE_LATIN_GLOBE_KEY_IGNORE_INTERVAL =
            "dev.lucky.gboardpatches.feature.latin_globe_key_ignore_interval";
    public static final String FEATURE_FEATURE_FLAGS_UI =
            "dev.lucky.gboardpatches.feature.feature_flags_ui";
    public static final String FEATURE_FLAG_ENABLE_NEW_LANGUAGE_SEARCH_BAR =
            "dev.lucky.gboardpatches.feature.flag_enable_new_language_search_bar";
    public static final String FEATURE_FLAG_ENABLE_SETTINGS_TWO_PANE =
            "dev.lucky.gboardpatches.feature.flag_enable_settings_two_pane";
    public static final String FEATURE_FLAG_SHOW_COLLAPSE_BUTTON =
            "dev.lucky.gboardpatches.feature.flag_show_collapse_button";
    public static final String FEATURE_KEY_SHAPE_CUSTOMIZATION =
            "dev.lucky.gboardpatches.feature.key_shape_customization";

    // Additional good flags discovered in decompiled Gboard (pdk registration + other smali)
    // from Rboard/GboardThemes community patterns and useful experimental features.
    public static final String FEATURE_FLAG_ENABLE_ADJUST_DEFAULT_KEYBOARD_HEIGHT =
            "dev.lucky.gboardpatches.feature.flag_enable_adjust_default_keyboard_height";
    public static final String FEATURE_FLAG_SILK_THEME =
            "dev.lucky.gboardpatches.feature.flag_silk_theme";
    public static final String FEATURE_FLAG_MATERIAL3_THEME =
            "dev.lucky.gboardpatches.feature.flag_material3_theme";
    public static final String FEATURE_FLAG_SILK_POPUP =
            "dev.lucky.gboardpatches.feature.flag_silk_popup";
    public static final String FEATURE_FLAG_ENABLE_AI_CORE_SMART_REPLY =
            "dev.lucky.gboardpatches.feature.flag_enable_ai_core_smart_reply";
    public static final String FEATURE_FLAG_KEYBOARD_REDESIGN_GOOGLE_SANS =
            "dev.lucky.gboardpatches.feature.flag_keyboard_redesign_google_sans";

    // New flags from Gboard 17.5.x (17.5.7.917159154)
    public static final String FEATURE_FLAG_ENABLE_WRITING_TOOLS =
            "dev.lucky.gboardpatches.feature.flag_enable_writing_tools";
    public static final String FEATURE_FLAG_ENABLE_WRITING_TOOLS_MY_STYLE =
            "dev.lucky.gboardpatches.feature.flag_enable_writing_tools_my_style";
    public static final String FEATURE_FLAG_ENABLE_WRITING_TOOLS_VOICE_COMMANDS =
            "dev.lucky.gboardpatches.feature.flag_enable_writing_tools_voice_commands";
    public static final String FEATURE_FLAG_ENABLE_WRITING_TOOLS_COOPERATIVE_MODE =
            "dev.lucky.gboardpatches.feature.flag_enable_writing_tools_cooperative_mode";
    public static final String FEATURE_FLAG_ENABLE_DYNAMIC_ART =
            "dev.lucky.gboardpatches.feature.flag_enable_dynamic_art";
    public static final String FEATURE_FLAG_ENABLE_DYNAMIC_ART_STICKER =
            "dev.lucky.gboardpatches.feature.flag_enable_dynamic_art_sticker";
    public static final String FEATURE_FLAG_ENABLE_AUTO_FLOAT_KEYBOARD =
            "dev.lucky.gboardpatches.feature.flag_enable_auto_float_keyboard";
    public static final String FEATURE_FLAG_ENABLE_AUTO_FLOAT_LANDSCAPE =
            "dev.lucky.gboardpatches.feature.flag_enable_auto_float_landscape";
    public static final String FEATURE_FLAG_ENABLE_AUTOFILL_IME =
            "dev.lucky.gboardpatches.feature.flag_enable_autofill_ime";
    public static final String FEATURE_FLAG_ENABLE_CUSTOM_STICKER_TAB =
            "dev.lucky.gboardpatches.feature.flag_enable_custom_sticker_tab";
    public static final String FEATURE_FLAG_ENABLE_ANIMATED_EMOJI_SUGGESTIONS =
            "dev.lucky.gboardpatches.feature.flag_enable_animated_emoji_suggestions";
    public static final String FEATURE_FLAG_ENABLE_CONTEXTUAL_GIF_SEARCH =
            "dev.lucky.gboardpatches.feature.flag_enable_contextual_gif_search";
    public static final String FEATURE_FLAG_ENABLE_DYNAMIC_FONT_SIZE =
            "dev.lucky.gboardpatches.feature.flag_enable_dynamic_font_size";
    public static final String FEATURE_FLAG_ENABLE_DYNAMIC_DIACRITIC_KEY =
            "dev.lucky.gboardpatches.feature.flag_enable_dynamic_diacritic_key";
    public static final String FEATURE_FLAG_ENABLE_BACKUP_PERSONAL_DICT =
            "dev.lucky.gboardpatches.feature.flag_enable_backup_personal_dict";
    public static final String FEATURE_FLAG_ENABLE_TEXT_PREVIEW =
            "dev.lucky.gboardpatches.feature.flag_enable_text_preview";
    public static final String FEATURE_FLAG_ENABLE_TABLET_LARGE =
            "dev.lucky.gboardpatches.feature.flag_enable_tablet_large";
    public static final String FEATURE_FLAG_ENABLE_VOICE_EDIT =
            "dev.lucky.gboardpatches.feature.flag_enable_voice_edit";
    public static final String FEATURE_FLAG_ENABLE_VOICE_CHIP_TOOLTIP =
            "dev.lucky.gboardpatches.feature.flag_enable_voice_chip_tooltip";
    public static final String FEATURE_FLAG_ENABLE_CLIPBOARD_ACTION_CHIPS =
            "dev.lucky.gboardpatches.feature.flag_enable_clipboard_action_chips";
    public static final String FEATURE_FLAG_ENABLE_CLIP_ITEM_CONSUMERS =
            "dev.lucky.gboardpatches.feature.flag_enable_clip_item_consumers";
    public static final String FEATURE_FLAG_ENABLE_CORRECTION_COMMIT_ANIM =
            "dev.lucky.gboardpatches.feature.flag_enable_correction_commit_anim";
    public static final String FEATURE_FLAG_ENABLE_ONBOARDING_BANNER =
            "dev.lucky.gboardpatches.feature.flag_enable_onboarding_banner";
    public static final String FEATURE_FLAG_ENABLE_ACCESS_POINTS_REDESIGN =
            "dev.lucky.gboardpatches.feature.flag_enable_access_points_redesign";
    public static final String FEATURE_FLAG_ENABLE_WRITING_TOOLS_STYLE =
            "dev.lucky.gboardpatches.feature.flag_enable_writing_tools_style";
    public static final String FEATURE_FLAG_ENABLE_WRITING_TOOLS_REPLACE =
            "dev.lucky.gboardpatches.feature.flag_enable_writing_tools_replace";
    public static final String FEATURE_FLAG_ENABLE_WRITING_TOOLS_FOR_MINORS =
            "dev.lucky.gboardpatches.feature.flag_enable_writing_tools_for_minors";

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
