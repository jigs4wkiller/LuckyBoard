package dev.lucky.gboardpatches.extension.featureflags;

import android.content.Context;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import dev.lucky.gboardpatches.extension.featureflags.GboardFeatureFlagsSettings;
import dev.lucky.gboardpatches.extension.settings.GboardPatchesFeatureAvailability;

@SuppressWarnings("unused")
public final class GboardFeatureFlagsRuntime {
    public static final String FLAG_CLIPBOARD_ENTITY_EXTRACTION =
            "enable_clipboard_entity_extraction";
    public static final String FLAG_CLIPBOARD_ITEM_EDIT =
            "enable_clipboard_text_editor";
    public static final String FLAG_GRAMMAR_CHECKER =
            "enable_grammar_checker";
    public static final String FLAG_INLINE_SUGGESTIONS =
            "enable_inline_suggestions_on_client_side";
    public static final String FLAG_KEY_SHAPE_SELECTION =
            "more_pill_keys";
    public static final String FLAG_SUPPORT_ACCESSORY_KEYBOARD =
            "support_accessory_keyboard";
    public static final String FLAG_ENABLE_VOICE_WIDGET =
            "enable_voice_widget";
    public static final String FLAG_ENABLE_NEW_LANGUAGE_SEARCH_BAR =
            "enable_new_language_search_bar";
    public static final String FLAG_ENABLE_SETTINGS_TWO_PANE =
            "enable_settings_two_pane_display";
    public static final String FLAG_ENABLE_OCR =
            "enable_ocr";
    public static final String FLAG_ENABLE_DICTATION_REDESIGN =
            "enable_regular_dictation_redesign";
    public static final String FLAG_SHOW_COLLAPSE_BUTTON =
            "show_collapse_button";

    // Additional flags from APK decompile (pdk.smali registrations + broader searches)
    // Useful for power users: keyboard sizing, modern themes (silk/material3), AI, redesigns.
    public static final String FLAG_ENABLE_ADJUST_DEFAULT_KEYBOARD_HEIGHT =
            "enable_adjust_default_keyboard_height";
    public static final String FLAG_SILK_THEME = "silk_theme";
    public static final String FLAG_MATERIAL3_THEME = "material3_theme";
    public static final String FLAG_SILK_POPUP = "silk_popup";
    public static final String FLAG_ENABLE_AI_CORE_SMART_REPLY = "enable_ai_core_smart_reply";
    public static final String FLAG_KEYBOARD_REDESIGN_GOOGLE_SANS = "keyboard_redesign_google_sans";
    public static final String FLAG_BRIGHT_KEY_ON_DYNAMIC_COLOR_DARK_THEME =
            "bright_key_on_dynamic_color_dark_theme";

    private static final Map<String, String> FLAG_TO_FEATURE_KEY =
            createFlagToFeatureKeyMap();
    private static final Map<String, Boolean> FEATURE_ENABLED_CACHE =
            Collections.synchronizedMap(new HashMap<String, Boolean>());

    private static volatile Context APPLICATION_CONTEXT;

    private GboardFeatureFlagsRuntime() {
    }

    public static boolean shouldForceFlagTrue(String flagName) {
        if (flagName == null || flagName.isEmpty()) {
            return false;
        }

        // Special case for key shape (deep dive into Rboard/GboardThemes community + Gboard decompiles):
        // Force only the known *boolean* flags that control pill/rounded/gradations (from community: "more_pill_keys", "pill_shaped_key" for pill keys; "belka_rounded_keyboard", "enable_rounded_key_by_default" etc. for rounded).
        // **Exact matches only** — broad contains("rounded") etc. would hit numeric flags (semi_rounded_key_radius_*) and return Boolean instead of the expected value object, causing crash when round/pill shape is chosen (common Rboard complaint too: pill breaks layouts/crashes with incompatible themes).
        // When our key_shape_* markers are present (set by "Key Shape Selection" patch or Feature Flags + more_pill_keys), force true.
        // This (plus default-on in prefs) should expand the Theme details > Key shape picker beyond the 3 basics (Keine/Rechteckig/Abgerundet) to include gradations (light/medium/strong/very strong/pill) + our custom less-rounded/horizontal-lines.
        // Community note: pill/round often requires theme updates for padding/drawables; Gboard betas have native pill experiments that were rolled back due to backlash.
        if ("more_pill_keys".equals(flagName) ||
            "pill_shaped_key".equals(flagName) ||
            "belka_rounded_keyboard".equals(flagName) ||
            "enable_rounded_key_by_default".equals(flagName) ||
            "enable_rounded_keys".equals(flagName) ||
            "rounded_key_banner".equals(flagName)) {
            Context context = applicationContext();
            if (context != null && (
                    GboardPatchesFeatureAvailability.hasFeature(context, GboardPatchesFeatureAvailability.FEATURE_KEY_SHAPE_CUSTOMIZATION) ||
                    GboardPatchesFeatureAvailability.hasFeature(context, GboardPatchesFeatureAvailability.FEATURE_KEY_SHAPE_SELECTION)
            )) {
                FEATURE_ENABLED_CACHE.put("key_shape_" + flagName, Boolean.TRUE);
                return true;
            }
        }

        String featureKey = FLAG_TO_FEATURE_KEY.get(flagName);
        if (featureKey == null) {
            return false;
        }

        Boolean cached = FEATURE_ENABLED_CACHE.get(featureKey);
        if (cached != null) {
            return cached.booleanValue();
        }

        Context context = applicationContext();
        if (context == null) {
            return false;
        }

        boolean staticEnabled = GboardPatchesFeatureAvailability.hasFeature(context, featureKey);
        if (!staticEnabled) {
            FEATURE_ENABLED_CACHE.put(featureKey, Boolean.FALSE);
            return false;
        }

        // If the in-app Feature Flags UI was patched in (via the top "Add flags to Lucky Settings" option),
        // respect the user's runtime toggle (defaults to true / the patch-time choice).
        if (GboardPatchesFeatureAvailability.hasFeature(
                context, GboardPatchesFeatureAvailability.FEATURE_FEATURE_FLAGS_UI)) {
            boolean userEnabled = GboardFeatureFlagsSettings.readFlagEnabled(context, flagName, false);
            FEATURE_ENABLED_CACHE.put(featureKey, Boolean.valueOf(userEnabled));
            return userEnabled;
        }

        FEATURE_ENABLED_CACHE.put(featureKey, Boolean.TRUE);
        return true;
    }

    private static Map<String, String> createFlagToFeatureKeyMap() {
        Map<String, String> featureKeys = new HashMap<String, String>();
        featureKeys.put(
                FLAG_CLIPBOARD_ENTITY_EXTRACTION,
                GboardPatchesFeatureAvailability.FEATURE_CLIPBOARD_ENTITY_EXTRACTION);
        featureKeys.put(
                FLAG_CLIPBOARD_ITEM_EDIT,
                GboardPatchesFeatureAvailability.FEATURE_CLIPBOARD_ITEM_EDIT);
        featureKeys.put(
                FLAG_GRAMMAR_CHECKER,
                GboardPatchesFeatureAvailability.FEATURE_GRAMMAR_CHECKER);
        featureKeys.put(
                FLAG_INLINE_SUGGESTIONS,
                GboardPatchesFeatureAvailability.FEATURE_INLINE_SUGGESTIONS);
        featureKeys.put(
                FLAG_KEY_SHAPE_SELECTION,
                GboardPatchesFeatureAvailability.FEATURE_KEY_SHAPE_SELECTION);
        featureKeys.put(
                FLAG_SUPPORT_ACCESSORY_KEYBOARD,
                GboardPatchesFeatureAvailability.FEATURE_FLAG_SUPPORT_ACCESSORY_KEYBOARD);
        featureKeys.put(
                FLAG_ENABLE_VOICE_WIDGET,
                GboardPatchesFeatureAvailability.FEATURE_FLAG_ENABLE_VOICE_WIDGET);
        featureKeys.put(
                FLAG_ENABLE_NEW_LANGUAGE_SEARCH_BAR,
                GboardPatchesFeatureAvailability.FEATURE_FLAG_ENABLE_NEW_LANGUAGE_SEARCH_BAR);
        featureKeys.put(
                FLAG_ENABLE_SETTINGS_TWO_PANE,
                GboardPatchesFeatureAvailability.FEATURE_FLAG_ENABLE_SETTINGS_TWO_PANE);
        featureKeys.put(
                FLAG_ENABLE_OCR,
                GboardPatchesFeatureAvailability.FEATURE_FLAG_ENABLE_OCR);
        featureKeys.put(
                FLAG_ENABLE_DICTATION_REDESIGN,
                GboardPatchesFeatureAvailability.FEATURE_FLAG_ENABLE_DICTATION_REDESIGN);
        featureKeys.put(
                FLAG_SHOW_COLLAPSE_BUTTON,
                GboardPatchesFeatureAvailability.FEATURE_FLAG_SHOW_COLLAPSE_BUTTON);

        // New ones from decompile
        featureKeys.put(
                FLAG_ENABLE_ADJUST_DEFAULT_KEYBOARD_HEIGHT,
                GboardPatchesFeatureAvailability.FEATURE_FLAG_ENABLE_ADJUST_DEFAULT_KEYBOARD_HEIGHT);
        featureKeys.put(
                FLAG_SILK_THEME,
                GboardPatchesFeatureAvailability.FEATURE_FLAG_SILK_THEME);
        featureKeys.put(
                FLAG_MATERIAL3_THEME,
                GboardPatchesFeatureAvailability.FEATURE_FLAG_MATERIAL3_THEME);
        featureKeys.put(
                FLAG_SILK_POPUP,
                GboardPatchesFeatureAvailability.FEATURE_FLAG_SILK_POPUP);
        featureKeys.put(
                FLAG_ENABLE_AI_CORE_SMART_REPLY,
                GboardPatchesFeatureAvailability.FEATURE_FLAG_ENABLE_AI_CORE_SMART_REPLY);
        featureKeys.put(
                FLAG_KEYBOARD_REDESIGN_GOOGLE_SANS,
                GboardPatchesFeatureAvailability.FEATURE_FLAG_KEYBOARD_REDESIGN_GOOGLE_SANS);
        featureKeys.put(
                FLAG_BRIGHT_KEY_ON_DYNAMIC_COLOR_DARK_THEME,
                GboardPatchesFeatureAvailability.FEATURE_FLAG_BRIGHT_KEY_ON_DYNAMIC_COLOR_DARK_THEME);
        return Collections.unmodifiableMap(featureKeys);
    }

    private static Context applicationContext() {
        Context context = APPLICATION_CONTEXT;
        if (context != null) {
            return context;
        }

        context = reflectedApplicationContext("android.app.ActivityThread", "currentApplication");
        if (context != null) {
            APPLICATION_CONTEXT = context;
            return context;
        }

        context = reflectedApplicationContext("android.app.AppGlobals", "getInitialApplication");
        if (context != null) {
            APPLICATION_CONTEXT = context;
        }
        return context;
    }

    private static Context reflectedApplicationContext(String className, String methodName) {
        try {
            Class<?> owner = Class.forName(className);
            Method method = owner.getDeclaredMethod(methodName);
            Object application = method.invoke(null);
            return application instanceof Context ? (Context) application : null;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
