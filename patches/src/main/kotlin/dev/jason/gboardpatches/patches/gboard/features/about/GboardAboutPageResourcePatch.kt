package dev.jason.gboardpatches.patches.gboard.features.about

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.gboard.shared.ANDROID_NS
import dev.jason.gboardpatches.patches.gboard.shared.childElements
import dev.jason.gboardpatches.patches.gboard.shared.insertAfter
import dev.jason.gboardpatches.patches.shared.Constants.GBOARD_PATCH_AUTHOR
import dev.jason.gboardpatches.patches.shared.Constants.GBOARD_PATCH_AUTHOR_URL
import dev.jason.gboardpatches.patches.shared.Constants.GBOARD_PATCH_REPOSITORY_URL
import dev.jason.gboardpatches.patches.shared.Constants.GBOARD_PATCH_VERSION
import org.w3c.dom.Document
import org.w3c.dom.Element

private const val ABOUT_XML = "res/xml/setting_about.xml"
private const val ABOUT_VERSION_KEY_REF = "@string/string_0x7f140b7d"
private const val ABOUT_PREFERENCE_CLASS =
    "com.google.android.libraries.inputmethod.settings.widget.ExtendedPreference"

internal val gboardAboutPageResourcePatch = resourcePatch(
    description = "在關於頁加入 patch 資訊。"
) {
    finalize {
        applyAboutPagePatch()
    }
}

context(ResourcePatchContext)
private fun applyAboutPagePatch() {
    document(ABOUT_XML).use { document ->
        val screen = document.documentElement
        val versionPreference = screen.childElements().firstOrNull {
            it.getAttributeNS(ANDROID_NS, "key") == ABOUT_VERSION_KEY_REF ||
                it.getAttribute("android:key") == ABOUT_VERSION_KEY_REF
        } ?: screen.childElements().lastOrNull()
        ?: error("Could not find any preference node in $ABOUT_XML")

        val authorPreference = ensureAboutPreference(
            document = document,
            key = "gboard_about_author",
            title = "Author",
            summary = GBOARD_PATCH_AUTHOR,
            intentUrl = GBOARD_PATCH_AUTHOR_URL
        )
        screen.insertAfter(authorPreference, versionPreference)

        val patchVersionPreference = ensureAboutPreference(
            document = document,
            key = "gboard_about_patch_version",
            title = "Patch Version",
            summary = GBOARD_PATCH_VERSION,
            intentUrl = GBOARD_PATCH_REPOSITORY_URL
        )
        screen.insertAfter(patchVersionPreference, authorPreference)
    }
}

private fun ensureAboutPreference(
    document: Document,
    key: String,
    title: String,
    summary: String,
    intentUrl: String? = null
): Element {
    val screen = document.documentElement
    val preference = screen.childElements().firstOrNull {
        it.tagName == ABOUT_PREFERENCE_CLASS &&
            (
                it.getAttributeNS(ANDROID_NS, "key") == key ||
                    it.getAttribute("android:key") == key
            )
    } ?: document.createElement(ABOUT_PREFERENCE_CLASS)

    preference.setAndroidAttribute("persistent", "false")
    preference.setAndroidAttribute("focusable", "false")
    preference.setAndroidAttribute("maxLines", "100")
    preference.setAndroidAttribute("title", title)
    preference.setAndroidAttribute("selectable", if (intentUrl != null) "true" else "false")
    preference.setAndroidAttribute("key", key)
    preference.setAndroidAttribute("summary", summary)
    intentUrl?.let { url ->
        preference.ensureIntent().apply {
            setAndroidAttribute("action", "android.intent.action.VIEW")
            setAndroidAttribute("data", url)
        }
    }
    return preference
}

private fun Element.setAndroidAttribute(localName: String, value: String) {
    setAttributeNS(ANDROID_NS, "android:$localName", value)
}

private fun Element.ensureIntent(): Element =
    childElements("intent").firstOrNull() ?: ownerDocument.createElement("intent").also {
        appendChild(it)
    }
