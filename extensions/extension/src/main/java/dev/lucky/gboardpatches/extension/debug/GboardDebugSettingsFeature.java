package dev.lucky.gboardpatches.extension.debug;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.lucky.gboardpatches.extension.settings.GboardPatchesFeatureAvailability;
import dev.lucky.gboardpatches.extension.settings.GboardPatchesSettingsContract;

public final class GboardDebugSettingsFeature
        implements GboardPatchesSettingsContract.Feature {
    private static final String ENTRY_TITLE = "Debug helper";
    private static final String ENTRY_SUMMARY = "Copy app log for debugging";
    private static final String HEADER_SUMMARY =
            "Copy the complete LuckyBoard app log to clipboard for debugging.";
    private static final String SECTION_TITLE = "Debug Tools";
    private static final String COPY_LOG_TITLE = "Copy App Log";
    private static final String COPY_LOG_SUMMARY = "Copy complete logcat to clipboard";

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
                GboardPatchesFeatureAvailability.FEATURE_DEBUG_PATCHES);
    }

    @Override
    public GboardPatchesSettingsContract.Screen buildScreen(
            GboardPatchesSettingsContract.Host host) {
        Context context = host.getContext();

        List<GboardPatchesSettingsContract.Row> rows = new ArrayList<>();
        rows.add(new GboardPatchesSettingsContract.CommandRow(
                COPY_LOG_TITLE,
                COPY_LOG_SUMMARY,
                true,
                () -> {
                    GboardDebugLogCollector.copyLogsToClipboard(context);
                    Toast.makeText(context, "Log copied to clipboard", Toast.LENGTH_SHORT).show();
                }));

        return new GboardPatchesSettingsContract.Screen(
                ENTRY_TITLE,
                "LuckyBoard",
                ENTRY_TITLE,
                HEADER_SUMMARY,
                Collections.emptyList(),
                Collections.singletonList(
                        new GboardPatchesSettingsContract.Section(SECTION_TITLE, rows)));
    }
}
