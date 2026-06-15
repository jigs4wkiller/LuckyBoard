package dev.lucky.gboardpatches.patches.gboard.registry

import dev.lucky.gboardpatches.patches.universal.universalDrawableCleanerPatch
import dev.lucky.gboardpatches.patches.universal.universalLanguageCleanerPatch
import dev.lucky.gboardpatches.patches.universal.universalPngOptimizerPatch

// Minimal registry for beta-pure-png-impl test branch.
// Only the universal patches, with the pure-JDK PNG optimizer (no ext libs).
// This allows a clean build for testing the new PNG impl without the full Gboard patch set.

val allPatches = listOf(
    universalDrawableCleanerPatch,
    universalLanguageCleanerPatch,
    universalPngOptimizerPatch,
)
