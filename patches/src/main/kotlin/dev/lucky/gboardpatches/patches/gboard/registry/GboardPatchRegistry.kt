package dev.lucky.gboardpatches.patches.gboard.registry

import dev.lucky.gboardpatches.patches.universal.universalDrawableCleanerPatch
import dev.lucky.gboardpatches.patches.universal.universalLanguageCleanerPatch
import dev.lucky.gboardpatches.patches.universal.universalPngOptimizerPatch

// Minimal registry for beta-pure-png-impl (pure JDK PNG optimizer test branch).
// Only registers the universal patches (with the new pure no-ext-lib PNG optimizer).
// This avoids all the unresolved references from the full Gboard patch set on this isolated branch.

val allPatches = listOf(
    universalDrawableCleanerPatch,
    universalLanguageCleanerPatch,
    universalPngOptimizerPatch,
)
