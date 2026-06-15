package dev.lucky.gboardpatches.extension.featureflags;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Runtime preferences for per-flag toggles, exposed in the in-app Lucky Settings UI
 * when the "Add flags to Lucky Settings" patch option is enabled.
 *
 * These override the static patch-time markers for the corresponding internal flags.
 */
public final class GboardFeatureFlagsSettings {

    private static final String PREFS_NAME = "dev.lucky.gboardpatches.feature_flags";
    private static final String PREF_PREFIX = "flag_";

    // Known flags with their internal flag name (used as key) and user-facing info.
    // Keep in sync with GboardFeatureFlagsPatch.kt options and the runtime map.
    public static final Map<String, FlagInfo> ALL_FLAGS;

    static {
        Map<String, FlagInfo> map = new HashMap<String, FlagInfo>();
        map.put("more_pill_keys", new FlagInfo("Key Shape Selection", "Enables the Key shape option in Theme details and extra gradations (less rounded, horizontal lines, pill etc.)."));
        map.put("support_accessory_keyboard", new FlagInfo("Accessory / Floating Keyboard", "Enables floating keyboard, accessory modes and related toolbar widgets."));
        map.put("enable_voice_widget", new FlagInfo("Voice Widget / Toolbar", "Enables enhanced voice dictation widget and toolbar integration."));
        map.put("enable_new_language_search_bar", new FlagInfo("New Language Search Bar", "Improved search for languages and input methods in LuckyBoard settings."));
        map.put("enable_settings_two_pane", new FlagInfo("Two-Pane Settings UI", "Uses a split / two-column layout for settings on larger screens."));
        map.put("enable_ocr", new FlagInfo("OCR / Scan Text", "Adds a camera-based 'Scan text' tool to the toolbar for OCR and quick insertion."));
        map.put("enable_regular_dictation_redesign", new FlagInfo("Voice Dictation Redesign", "Modernized voice input UI with better visual treatment."));
        map.put("show_collapse_button", new FlagInfo("Collapse Keyboard Button", "Shows a button to minimize/collapse the on-screen keyboard."));
        map.put("enable_grammar_checker", new FlagInfo("Grammar Checker", "Enables advanced grammar checking under text correction."));
        map.put("enable_inline_suggestions_on_client_side", new FlagInfo("Inline Suggestions", "Enables Smart Compose and inline text suggestions."));
        map.put("enable_clipboard_entity_extraction", new FlagInfo("Clipboard Entity Extraction", "Shows detected addresses, phones, links etc. from clipboard items."));
        map.put("enable_clipboard_text_editor", new FlagInfo("Clipboard Item Edit", "Allows editing clipboard items directly from the long-press menu."));

        // New flags from decompiled Gboard APK exploration (pdk registrations + community Rboard patterns).
        map.put("enable_adjust_default_keyboard_height", new FlagInfo("Adjust Default Keyboard Height", "Allow adjusting the default on-screen keyboard height."));
        map.put("silk_theme", new FlagInfo("Silk Theme", "Enable Silk / smooth Material You theme effects."));
        map.put("material3_theme", new FlagInfo("Material 3 Theme", "Enable Material 3 theming in Gboard."));
        map.put("silk_popup", new FlagInfo("Silk Popups", "Use refined Silk-style popups and suggestions."));
        map.put("enable_ai_core_smart_reply", new FlagInfo("AI Smart Reply", "Enable AI core smart reply suggestions."));
        map.put("keyboard_redesign_google_sans", new FlagInfo("Keyboard Redesign Google Sans", "Apply Google Sans in modern keyboard redesign layouts."));
        map.put("bright_key_on_dynamic_color_dark_theme", new FlagInfo("Bright Keys on Dynamic Color Dark", "Use brighter key colors with dynamic color dark themes."));
        ALL_FLAGS = Collections.unmodifiableMap(map);
    }

    private GboardFeatureFlagsSettings() {
    }

    public static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean readFlagEnabled(Context context, String internalFlagName, boolean defaultValue) {
        SharedPreferences prefs = preferences(context);
        String key = PREF_PREFIX + internalFlagName;
        if (!prefs.contains(key)) {
            // For key shape related flags, default to enabled (true) so the extended shapes
            // (more gradations, less rounded, horizontal lines) appear immediately when the
            // Key Shape Selection patch or the more_pill_keys option is active.
            // User can still turn them off in the in-app flags UI if desired.
            if ("more_pill_keys".equals(internalFlagName) ||
                internalFlagName.contains("rounded") ||
                internalFlagName.contains("pill")) {
                defaultValue = true;
                prefs.edit().putBoolean(key, true).apply();
            }
        }
        return prefs.getBoolean(key, defaultValue);
    }

    public static void writeFlagEnabled(Context context, String internalFlagName, boolean enabled) {
        SharedPreferences prefs = preferences(context);
        String key = PREF_PREFIX + internalFlagName;
        prefs.edit().putBoolean(key, enabled).apply();
    }

    public static class FlagInfo {
        public final String title;
        public final String description;

        public FlagInfo(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }
}
