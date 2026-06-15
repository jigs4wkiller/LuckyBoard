package dev.jason.gboardpatches.extension.featureflags;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import dev.jason.gboardpatches.extension.settings.GboardPatchesFeatureAvailability;
import dev.jason.gboardpatches.extension.settings.GboardPatchesSettingsContract;

public final class GboardFeatureFlagsSettingsFeature
        implements GboardPatchesSettingsContract.Feature {

    private static final String TAG = "GboardFeatureFlags";
    private static final String ENTRY_TITLE = "Feature Flags";
    private static final String ENTRY_SUMMARY =
            "Toggle LuckyBoard internal rollout flags at runtime. Some changes (e.g. key shapes, incognito) require restarting LuckyBoard to take effect.";
    private static final String HEADER_BADGE = "LuckyBoard";
    private static final String HEADER_TITLE = "Feature Flags";
    private static final String HEADER_SUMMARY =
            "These flags appear because they were selected during patching. They start DISABLED. Flip the switches ON here to activate the corresponding LuckyBoard features. Each flag has a short description. Use the Restart button below for changes that need a full LuckyBoard restart.";

    @Override
    public String getEntryTitle() {
        return ENTRY_TITLE;
    }

    @Override
    public String getEntrySummary() {
        return ENTRY_SUMMARY;
    }

    @Override
    public boolean isAvailable(Context context) {
        return GboardPatchesFeatureAvailability.hasFeature(
                context, GboardPatchesFeatureAvailability.FEATURE_FEATURE_FLAGS_UI);
    }

    @Override
    public GboardPatchesSettingsContract.Screen buildScreen(
            GboardPatchesSettingsContract.Host host) {
        Context context = host.getContext();
        SharedPreferences prefs = GboardFeatureFlagsSettings.preferences(context);

        List<GboardPatchesSettingsContract.Row> rows =
                new ArrayList<GboardPatchesSettingsContract.Row>();

        // Iterate known flags. Only show those that have their patch-time marker present.
        for (java.util.Map.Entry<String, GboardFeatureFlagsSettings.FlagInfo> entry :
                GboardFeatureFlagsSettings.ALL_FLAGS.entrySet()) {

            String internalName = entry.getKey();
            GboardFeatureFlagsSettings.FlagInfo info = entry.getValue();

            // Check if this flag was included at patch time (its marker is set).
            // We check both the modern flag_ marker and legacy ones for the consolidated flags.
            String modernMarker = "dev.jason.gboardpatches.feature.flag_" + internalName.replace("_", "_");
            // Simpler: try the common patterns + specific legacy markers
            boolean available = GboardPatchesFeatureAvailability.hasAnyFeature(
                    context,
                    "dev.jason.gboardpatches.feature.flag_" + internalName,
                    getLegacyMarkerFor(internalName));

            if (!available) {
                continue;
            }

            boolean checked = GboardFeatureFlagsSettings.readFlagEnabled(context, internalName, false);

            rows.add(new GboardPatchesSettingsContract.SwitchRow(
                    info.title,
                    info.description,
                    true,
                    checked,
                    value -> GboardFeatureFlagsSettings.writeFlagEnabled(context, internalName, value)
            ));
        }

        if (rows.isEmpty()) {
            rows.add(new GboardPatchesSettingsContract.InfoRow(
                    "No flags available",
                    "No feature flags were enabled at patch time for this build, or the UI was not included.",
                    false));
        } else {
            // Add restart button - flag changes often require LuckyBoard restart to fully apply in the picker/UI
            rows.add(new GboardPatchesSettingsContract.CommandRow(
                    "Restart LuckyBoard to apply",
                    "Most flag toggles (especially key shapes, incognito, voice etc.) require a full restart of LuckyBoard to take effect.",
                    true,
                    () -> restartGboard(context)
            ));
        }

        return new GboardPatchesSettingsContract.Screen(
                "Flags",
                HEADER_BADGE,
                HEADER_TITLE,
                HEADER_SUMMARY,
                rows
        );
    }

    private static String getLegacyMarkerFor(String internalName) {
        // Map the few consolidated legacy flags to their original markers
        if ("enable_grammar_checker".equals(internalName)) {
            return GboardPatchesFeatureAvailability.FEATURE_GRAMMAR_CHECKER;
        }
        if ("enable_inline_suggestions_on_client_side".equals(internalName)) {
            return GboardPatchesFeatureAvailability.FEATURE_INLINE_SUGGESTIONS;
        }
        if ("enable_clipboard_entity_extraction".equals(internalName)) {
            return GboardPatchesFeatureAvailability.FEATURE_CLIPBOARD_ENTITY_EXTRACTION;
        }
        if ("enable_clipboard_text_editor".equals(internalName)) {
            return GboardPatchesFeatureAvailability.FEATURE_CLIPBOARD_ITEM_EDIT;
        }
        if ("more_pill_keys".equals(internalName)) {
            return GboardPatchesFeatureAvailability.FEATURE_KEY_SHAPE_SELECTION;
        }
        return "dev.jason.gboardpatches.feature." + internalName; // fallback
    }

    private void restartGboard(Context context) {
        try {
            android.widget.Toast.makeText(context, "Restarting LuckyBoard...", android.widget.Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {}
        // Kill the current LuckyBoard process. It will restart automatically on next keyboard use.
        // This is the standard way in these patches to apply flag changes.
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            try {
                android.os.Process.killProcess(android.os.Process.myPid());
            } catch (Exception ignored) {}
        }, 600);
    }
}
