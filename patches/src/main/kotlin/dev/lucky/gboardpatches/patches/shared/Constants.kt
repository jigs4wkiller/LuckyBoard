package dev.lucky.gboardpatches.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    const val GBOARD_PACKAGE_NAME = "com.google.android.inputmethod.latin"
    const val GBOARD_PATCHED_PACKAGE_NAME = "dev.lucky.com.google.android.inputmethod.latin"
    const val LUCKYBOARD_APP_NAME = "LuckyBoard"
    const val GBOARD_PATCH_AUTHOR = "jigs4wkiller"
    const val GBOARD_PATCH_AUTHOR_URL = "https://github.com/jigs4wkiller"
    const val GBOARD_PATCH_REPOSITORY_URL = "https://github.com/jigs4wkiller/LuckyBoard"
    val GBOARD_PATCH_VERSION = PatchBuildInfo.VERSION

    val COMPATIBILITY_GBOARD = Compatibility(
        name = "Gboard",
        packageName = GBOARD_PACKAGE_NAME,
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1A73E8,
        signatures = setOf(
            "7ce83c1b71f3d572fed04c8d40c5cb10ff75e6d87d9df6fbd53f0468c2905053",
            "f0fd6c5b410f25cb25c3b53346c8972fae30f8ee7411df910480ad6b2d60db83"
        ),
        targets = listOf(
            AppTarget(
                version = "17.0.10.880768217-release-arm64-v8a",
                isExperimental = false
            )
        )
    )
}
