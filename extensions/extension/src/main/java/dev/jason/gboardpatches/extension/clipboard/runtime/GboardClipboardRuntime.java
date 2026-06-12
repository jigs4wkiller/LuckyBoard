package dev.jason.gboardpatches.extension.clipboard;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import dev.jason.gboardpatches.extension.clipboard.runtime.features.GboardClipboardColumnCountFeature;
import dev.jason.gboardpatches.extension.clipboard.runtime.features.GboardClipboardCountdownFeature;
import dev.jason.gboardpatches.extension.clipboard.runtime.features.GboardClipboardCreationTimeFeature;
import dev.jason.gboardpatches.extension.clipboard.runtime.features.GboardClipboardMaxCountFeature;
import dev.jason.gboardpatches.extension.clipboard.runtime.features.GboardClipboardOrderIndexFeature;
import dev.jason.gboardpatches.extension.clipboard.runtime.features.GboardClipboardPreviewLinesFeature;
import dev.jason.gboardpatches.extension.clipboard.runtime.features.GboardClipboardRetentionFeature;
import dev.jason.gboardpatches.extension.clipboard.runtime.hooks.GboardClipboardColumnCountHookAdapter;
import dev.jason.gboardpatches.extension.clipboard.runtime.hooks.GboardClipboardLoaderHookAdapter;
import dev.jason.gboardpatches.extension.clipboard.runtime.hooks.GboardClipboardPruneHookAdapter;
import dev.jason.gboardpatches.extension.clipboard.runtime.hooks.GboardClipboardUiHookAdapter;

@SuppressWarnings("unused")
public final class GboardClipboardRuntime {
    public static final String SETTINGS_PREF_FILE = "gboard_clipboard";
    public static final String LEGACY_SETTINGS_PREF_FILE = "gboard_clipboard_retention";

    private static final GboardClipboardRuntimeSupport SUPPORT =
            new GboardClipboardRuntimeSupport();
    private static final GboardClipboardRetentionFeature RETENTION_FEATURE =
            new GboardClipboardRetentionFeature(SUPPORT);
    private static final GboardClipboardMaxCountFeature MAX_COUNT_FEATURE =
            new GboardClipboardMaxCountFeature(SUPPORT);
    private static final GboardClipboardPreviewLinesFeature PREVIEW_LINES_FEATURE =
            new GboardClipboardPreviewLinesFeature(SUPPORT);
    private static final GboardClipboardCountdownFeature COUNTDOWN_FEATURE =
            new GboardClipboardCountdownFeature(SUPPORT, RETENTION_FEATURE);
    private static final GboardClipboardCreationTimeFeature CREATION_TIME_FEATURE =
            new GboardClipboardCreationTimeFeature(SUPPORT);
    private static final GboardClipboardOrderIndexFeature ORDER_INDEX_FEATURE =
            new GboardClipboardOrderIndexFeature(SUPPORT);
    private static final GboardClipboardColumnCountFeature COLUMN_COUNT_FEATURE =
            new GboardClipboardColumnCountFeature(SUPPORT);
    private static final GboardClipboardLoaderHookAdapter LOADER_HOOK_ADAPTER =
            new GboardClipboardLoaderHookAdapter(SUPPORT, RETENTION_FEATURE, MAX_COUNT_FEATURE);
    private static final GboardClipboardPruneHookAdapter PRUNE_HOOK_ADAPTER =
            new GboardClipboardPruneHookAdapter(SUPPORT, RETENTION_FEATURE, MAX_COUNT_FEATURE);
    private static final GboardClipboardUiHookAdapter UI_HOOK_ADAPTER =
            new GboardClipboardUiHookAdapter(SUPPORT, MAX_COUNT_FEATURE, PREVIEW_LINES_FEATURE,
                    COUNTDOWN_FEATURE, CREATION_TIME_FEATURE, ORDER_INDEX_FEATURE,
                    LOADER_HOOK_ADAPTER);
    private static final GboardClipboardColumnCountHookAdapter COLUMN_COUNT_HOOK_ADAPTER =
            new GboardClipboardColumnCountHookAdapter(COLUMN_COUNT_FEATURE);

    private GboardClipboardRuntime() {
    }

    private static boolean hasFeatureMarker(Context context, String feature) {
        if (context == null) return false;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(
                context.getPackageName(), PackageManager.GET_META_DATA);
            return ai.metaData != null && ai.metaData.getBoolean("dev.jason.gboardpatches.feature.clipboard_" + feature, false);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isClipboardEnabled() {
        return SUPPORT.isClipboardEnabled();
    }

    public static boolean shouldShowExpiryCountdown() {
        return hasFeatureMarker(SUPPORT.applicationContext(), "countdown") 
            && SUPPORT.runtimeSettings().showExpiryCountdown;
    }

    public static boolean shouldShowCreationTime() {
        return hasFeatureMarker(SUPPORT.applicationContext(), "creation_time")
            && SUPPORT.runtimeSettings().showCreationTime;
    }

    public static int configuredRetentionTtlMinutes() {
        return SUPPORT.configuredRetentionTtlMinutes();
    }

    public static int configuredMaxCount() {
        return SUPPORT.configuredMaxCount();
    }

    public static int configuredPreviewLines() {
        if (!hasFeatureMarker(SUPPORT.applicationContext(), "preview_lines")) {
            return GboardClipboardRuntimeSupport.STOCK_CLIPBOARD_CONTENT_MAX_LINES;
        }
        return SUPPORT.configuredPreviewLines();
    }

    public static boolean shouldEnableOrderIndex() {
        return hasFeatureMarker(SUPPORT.applicationContext(), "order_index")
            && SUPPORT.runtimeSettings().showOrderIndex;
    }

    public static boolean shouldEnableColumnCount() {
        return hasFeatureMarker(SUPPORT.applicationContext(), "column_count");
    }

    public static void registerApplicationContext(Context context) {
        SUPPORT.registerApplicationContext(context);
    }

    public static Object maybeBuildLoaderResult(Object receiver) {
        return LOADER_HOOK_ADAPTER.maybeBuildLoaderResult(receiver);
    }

    public static boolean handleCustomPrune(Object receiver) {
        return PRUNE_HOOK_ADAPTER.handleCustomPrune(receiver);
    }

    public static void afterAdapterTrim(Object receiver) {
        UI_HOOK_ADAPTER.afterAdapterTrim(receiver);
    }

    public static void afterItemBind(Object receiver, Object holderObject, int position) {
        UI_HOOK_ADAPTER.afterItemBind(receiver, holderObject, position);
    }

    public static Integer resolveColumnCountOverride(Object receiver) {
        if (!shouldEnableColumnCount()) return null;
        return COLUMN_COUNT_HOOK_ADAPTER.resolveColumnCountOverride(receiver);
    }
}
