package dev.lucky.gboardpatches.extension.settings;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.lucky.gboardpatches.extension.clipboard.GboardClipboardSettingsFeature;
import dev.lucky.gboardpatches.extension.featureflags.GboardFeatureFlagsSettingsFeature;
import dev.lucky.gboardpatches.extension.incognito.GboardIncognitoSettingsFeature;
import dev.lucky.gboardpatches.extension.keyboard.GboardKeyboardSettingsGroupFeature;
import dev.lucky.gboardpatches.extension.settingshomepage.GboardSettingsHomepageSettingsFeature;

public final class GboardPatchesSettingsFeatureRegistry {
    private static final String TAG = "GboardPatches";

    private GboardPatchesSettingsFeatureRegistry() {
    }

    public static List<GboardPatchesSettingsContract.Feature> features(Context context) {
        List<GboardPatchesSettingsContract.Feature> features =
                new ArrayList<GboardPatchesSettingsContract.Feature>();
        addIfAvailable(context, features, new GboardKeyboardSettingsGroupFeature(context));
        addIfAvailable(context, features, new GboardClipboardSettingsFeature());
        addIfAvailable(context, features, new GboardIncognitoSettingsFeature());
        addIfAvailable(context, features, new GboardFeatureFlagsSettingsFeature());
        addIfAvailable(context, features, new GboardSettingsHomepageSettingsFeature());
        return Collections.unmodifiableList(features);
    }

    private static void addIfAvailable(Context context,
            List<GboardPatchesSettingsContract.Feature> features,
            GboardPatchesSettingsContract.Feature feature) {
        try {
            if (feature.isAvailable(context)) {
                features.add(feature);
            }
        } catch (Throwable throwable) {
            Log.w(TAG, "Failed to evaluate feature availability: " + feature.getClass().getName(),
                    throwable);
        }
    }
}
