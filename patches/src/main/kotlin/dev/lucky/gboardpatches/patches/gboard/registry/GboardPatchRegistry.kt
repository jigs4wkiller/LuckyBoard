package dev.lucky.gboardpatches.patches.gboard.registry

import dev.lucky.gboardpatches.patches.universal.universalPngOptimizerPatch

// Ultra-minimal registry for beta-pure-png-impl (pure JDK PNG optimizer test branch).
// Only the pure no-ext-lib PNG optimizer patch.
// This branch is only for testing the new PNG impl, so no need for other patches.

val allPatches = listOf(
    universalPngOptimizerPatch,
)
