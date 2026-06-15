package dev.lucky.gboardpatches.patches.gboard.registry

import dev.lucky.gboardpatches.patches.universal.universalPngOptimizerPatch

// beta-pure-png-impl branch: focus on companion delegation PNG optimizer.
// Registry minimal so .mpp only ships the universal PNG patch + pure fallback/delegation.
// Other patches live on main. New approach = companion APK does the heavy opt "auf andere Weise".

val allPatches = listOf(
    universalPngOptimizerPatch,
)
