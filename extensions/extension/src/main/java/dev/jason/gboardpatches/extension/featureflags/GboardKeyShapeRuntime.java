package dev.jason.gboardpatches.extension.featureflags;

import android.content.Context;

import dev.jason.gboardpatches.extension.settings.GboardPatchesFeatureAvailability;

/**
 * Runtime support for extended Key Shape customization.
 *
 * When the customization feature marker is present (via the Key Shape Selection patch),
 * additional gradations become available in Gboard's Theme details > Key shape picker:
 *
 * - Less rounded (subtle / light curve) — a gentle roundness step before the stronger rounded options.
 * - Horizontal lines — flat rectangular keys with only subtle horizontal line accents (visual gradation
 *   toward a completely flat/minimal design).
 * - Plus the rounded gradations (light/medium/strong/very strong/pill) unlocked by the base selection flag.
 *
 * The actual population of extra shapes in the picker is primarily driven by Gboard's internal
 * handling of "more_pill_keys" (forced via the feature flag patch) + presence of the customization
 * marker (for extension-side awareness and potential future injection hooks).
 */
@SuppressWarnings("unused")
public final class GboardKeyShapeRuntime {

    private GboardKeyShapeRuntime() {}

    public static boolean isKeyShapeCustomizationEnabled(Context context) {
        return GboardPatchesFeatureAvailability.hasFeature(
                context, GboardPatchesFeatureAvailability.FEATURE_KEY_SHAPE_CUSTOMIZATION);
    }

    public static boolean isKeyShapeSelectionEnabled(Context context) {
        return GboardPatchesFeatureAvailability.hasFeature(
                context, GboardPatchesFeatureAvailability.FEATURE_KEY_SHAPE_SELECTION);
    }

    // Placeholder for future: methods to return additional custom shape descriptors,
    // corner radii, or path data for "less_rounded" and "horizontal_lines" styles if
    // we hook the internal shape provider list.
}
