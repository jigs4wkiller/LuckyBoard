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
        map.put("enable_clipboard_entity_extraction", new FlagInfo("Clipboard Entity Extraction", "Shows detected addresses, phones, links etc. from clipboard items."));
        map.put("enable_grammar_checker", new FlagInfo("Grammar Checker", "Enables advanced grammar checking under text correction."));
        map.put("enable_inline_suggestions_on_client_side", new FlagInfo("Inline Suggestions", "Enables Smart Compose and inline text suggestions."));
        map.put("more_pill_keys", new FlagInfo("Key Shape Selection", "Enables the Key shape option in Theme details and extra gradations (less rounded, horizontal lines, pill etc.)."));
        map.put("enable_new_language_search_bar", new FlagInfo("New Language Search Bar", "Improved search for languages and input methods in LuckyBoard settings."));
        map.put("enable_settings_two_pane", new FlagInfo("Two-Pane Settings UI", "Uses a split / two-column layout for settings on larger screens."));
        map.put("show_collapse_button", new FlagInfo("Collapse Keyboard Button", "Shows a button to minimize/collapse the on-screen keyboard."));

        // New flags from decompiled Gboard APK exploration (pdk registrations + community Rboard patterns).
        map.put("enable_adjust_default_keyboard_height", new FlagInfo("Adjust Default Keyboard Height", "Allow adjusting the default on-screen keyboard height."));
        map.put("silk_theme", new FlagInfo("Silk Theme", "Enable Silk / smooth Material You theme effects."));
        map.put("material3_theme", new FlagInfo("Material 3 Theme", "Enable Material 3 theming in Gboard."));
        map.put("silk_popup", new FlagInfo("Silk Popups", "Use refined Silk-style popups and suggestions."));
        map.put("enable_ai_core_smart_reply", new FlagInfo("AI Smart Reply", "Enable AI core smart reply suggestions."));
        map.put("keyboard_redesign_google_sans", new FlagInfo("Keyboard Redesign Google Sans", "Apply Google Sans in modern keyboard redesign layouts."));

        // New flags from Gboard 17.5.x
        map.put("enable_writing_tools", new FlagInfo("Writing Tools", "Enable AI-powered writing tools for text editing and style suggestions."));
        map.put("enable_writing_tools_my_style", new FlagInfo("Writing Tools My Style", "Enable personal style preservation in writing tools."));
        map.put("enable_writing_tools_voice_commands", new FlagInfo("Writing Tools Voice Commands", "Enable voice commands for writing tools."));
        map.put("enable_writing_tools_cooperative_mode", new FlagInfo("Writing Tools Cooperative Mode", "Enable cooperative mode for writing tools integration."));
        map.put("enable_dynamic_art", new FlagInfo("Dynamic Art Stickers", "Enable dynamic art sticker generation and suggestions."));
        map.put("enable_add_punctuation_into_dynamic_art_sticker", new FlagInfo("Dynamic Art Punctuation", "Add punctuation support to dynamic art stickers."));
        map.put("enable_auto_float_keyboard_in_freeform", new FlagInfo("Auto Float Keyboard", "Automatically float keyboard in freeform window mode."));
        map.put("enable_auto_float_keyboard_in_landscape", new FlagInfo("Auto Float Landscape", "Automatically float keyboard in landscape orientation."));
        map.put("enable_autofill_ime_integration", new FlagInfo("Autofill IME Integration", "Enable autofill integration with input method."));
        map.put("enable_custom_sticker_tab", new FlagInfo("Custom Sticker Tab", "Enable custom sticker tab in emoji panel."));
        map.put("enable_animated_emoji_content_suggestions", new FlagInfo("Animated Emoji Suggestions", "Enable animated emoji content suggestions."));
        map.put("enable_contextual_gif_search_query_suggestion", new FlagInfo("Contextual GIF Search", "Enable contextual GIF search query suggestions."));
        map.put("enable_dynamic_font_size_slider", new FlagInfo("Dynamic Font Size Slider", "Enable dynamic font size adjustment slider."));
        map.put("enable_dynamic_diacritic_key", new FlagInfo("Dynamic Diacritic Key", "Enable dynamic diacritic key behavior."));
        map.put("enable_backup_personal_dictionary", new FlagInfo("Backup Personal Dictionary", "Enable backup of personal dictionary entries."));
        map.put("enable_text_preview", new FlagInfo("Text Preview", "Enable text preview before commit."));
        map.put("enable_tablet_large", new FlagInfo("Tablet Large Mode", "Enable large tablet mode optimizations."));
        map.put("enable_voice_edit_in_rd", new FlagInfo("Voice Edit", "Enable voice editing in recognition display."));
        map.put("enable_voice_chip_tooltip", new FlagInfo("Voice Chip Tooltip", "Enable tooltip for voice input chip."));
        map.put("enable_clipboard_action_chips", new FlagInfo("Clipboard Action Chips", "Enable action chips for clipboard items."));
        map.put("enable_clip_item_consumers", new FlagInfo("Clipboard Item Consumers", "Enable clipboard item consumer integrations."));
        map.put("enable_correction_commit_animation", new FlagInfo("Correction Commit Animation", "Enable animation when committing corrections."));
        map.put("enable_common_onboarding_banner", new FlagInfo("Onboarding Banner", "Enable common onboarding banner."));
        map.put("enable_access_points_menu_redesign", new FlagInfo("Access Points Menu Redesign", "Enable redesigned access points menu."));
        map.put("enable_writing_tools_suggest_style", new FlagInfo("Writing Tools Style Suggestions", "Enable style suggestions in writing tools."));
        map.put("enable_writing_tools_replace_button", new FlagInfo("Writing Tools Replace Button", "Enable replace button in writing tools."));
        map.put("enable_writing_tools_for_minors", new FlagInfo("Writing Tools for Minors", "Enable writing tools for minor users."));
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
