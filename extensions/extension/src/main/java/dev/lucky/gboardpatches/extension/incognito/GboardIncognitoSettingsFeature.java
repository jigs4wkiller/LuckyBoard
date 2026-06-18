package dev.lucky.gboardpatches.extension.incognito;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.lucky.gboardpatches.extension.settings.GboardPatchesFeatureAvailability;
import dev.lucky.gboardpatches.extension.settings.GboardPatchesSettingsContract;

public final class GboardIncognitoSettingsFeature
        implements GboardPatchesSettingsContract.Feature {
    private static final String ENTRY_TITLE = "Incognito";
    private static final String ENTRY_SUMMARY = "Enable or disable incognito mode enhancements.";
    private static final String HEADER_SUMMARY =
            "Control incognito mode behavior. When enabled, Gboard forces incognito mode"
                    + " and allows clipboard and voice typing in incognito.";
    private static final String SECTION_TITLE = "Incognito Mode";
    private static final String SECTION_RESTART = "Apply Changes";
    private static final String RESTART_TITLE = "Restart LuckyBoard";
    private static final String RESTART_SUMMARY = "Restart to apply incognito changes";
    private static final String FORCE_INCOGNITO_TITLE = "Force Incognito";
    private static final String FORCE_INCOGNITO_SUMMARY = "Always open Gboard in incognito mode";
    private static final String ALLOW_CLIPBOARD_TITLE = "Clipboard in Incognito";
    private static final String ALLOW_CLIPBOARD_SUMMARY = "Allow clipboard access in incognito mode";
    private static final String ALLOW_VOICE_TITLE = "Voice in Incognito";
    private static final String ALLOW_VOICE_SUMMARY = "Allow voice typing in incognito mode";

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
                context,
                GboardPatchesFeatureAvailability.FEATURE_INCognito);
    }

    @Override
    public GboardPatchesSettingsContract.Screen buildScreen(
            GboardPatchesSettingsContract.Host host) {
        Context context = host.getContext();

        boolean forceIncognito = GboardIncognitoSettings.isForceIncognitoEnabled(context);
        boolean allowClipboard = GboardIncognitoSettings.isAllowClipboardEnabled(context);
        boolean allowVoice = GboardIncognitoSettings.isAllowVoiceEnabled(context);

        List<GboardPatchesSettingsContract.Row> rows = new ArrayList<>();
        rows.add(new GboardPatchesSettingsContract.SelectorRow(
                FORCE_INCOGNITO_TITLE,
                FORCE_INCOGNITO_SUMMARY,
                forceIncognito ? "Enabled" : "Disabled",
                true,
                () -> host.showChoiceDialog(
                        FORCE_INCOGNITO_TITLE,
                        new String[]{"Enabled", "Disabled"},
                        new String[]{"true", "false"},
                        String.valueOf(forceIncognito),
                        null,
                        () -> {},
                        value -> GboardIncognitoSettings.setForceIncognitoEnabled(
                                context, Boolean.parseBoolean(value)))));
        rows.add(new GboardPatchesSettingsContract.SelectorRow(
                ALLOW_CLIPBOARD_TITLE,
                ALLOW_CLIPBOARD_SUMMARY,
                allowClipboard ? "Enabled" : "Disabled",
                true,
                () -> host.showChoiceDialog(
                        ALLOW_CLIPBOARD_TITLE,
                        new String[]{"Enabled", "Disabled"},
                        new String[]{"true", "false"},
                        String.valueOf(allowClipboard),
                        null,
                        () -> {},
                        value -> GboardIncognitoSettings.setAllowClipboardEnabled(
                                context, Boolean.parseBoolean(value)))));
        rows.add(new GboardPatchesSettingsContract.SelectorRow(
                ALLOW_VOICE_TITLE,
                ALLOW_VOICE_SUMMARY,
                allowVoice ? "Enabled" : "Disabled",
                true,
                () -> host.showChoiceDialog(
                        ALLOW_VOICE_TITLE,
                        new String[]{"Enabled", "Disabled"},
                        new String[]{"true", "false"},
                        String.valueOf(allowVoice),
                        null,
                        () -> {},
                        value -> GboardIncognitoSettings.setAllowVoiceEnabled(
                                context, Boolean.parseBoolean(value)))));

        List<GboardPatchesSettingsContract.Row> restartRows = new ArrayList<>();
        restartRows.add(new GboardPatchesSettingsContract.CommandRow(
                RESTART_TITLE,
                RESTART_SUMMARY,
                true,
                () -> restartGboard(context)));

        return new GboardPatchesSettingsContract.Screen(
                ENTRY_TITLE,
                "LuckyBoard",
                ENTRY_TITLE,
                HEADER_SUMMARY,
                Collections.emptyList(),
                Arrays.asList(
                        new GboardPatchesSettingsContract.Section(SECTION_TITLE, rows),
                        new GboardPatchesSettingsContract.Section(SECTION_RESTART, restartRows)));
    }

    private void restartGboard(Context context) {
        try {
            android.widget.Toast.makeText(context, "Restarting LuckyBoard...",
                    android.widget.Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {}
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            try {
                android.os.Process.killProcess(android.os.Process.myPid());
            } catch (Exception ignored) {}
        }, 500);
    }
}
