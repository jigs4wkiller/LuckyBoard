package dev.jason.gboardpatches.patches.gboard

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD
import dev.jason.gboardpatches.patches.shared.Constants.GBOARD_PACKAGE_NAME
import dev.jason.gboardpatches.patches.shared.Constants.GBOARD_PATCHED_PACKAGE_NAME
import dev.jason.gboardpatches.patches.shared.Constants.GBOARD_PATCH_AUTHOR
import dev.jason.gboardpatches.patches.shared.Constants.GBOARD_PATCH_AUTHOR_URL
import dev.jason.gboardpatches.patches.shared.Constants.GBOARD_PATCH_REPOSITORY_URL
import dev.jason.gboardpatches.patches.shared.Constants.GBOARD_PATCH_VERSION
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

private const val ZHUYIN_TEMPLATE_XML = "res/xml/xml_0x7f17117a.xml"
private const val ZHUYIN_KEYS_XML = "res/xml/xml_0x7f171179.xml"
private const val ENGLISH_QWERTY_XML = "res/xml/xml_0x7f171175.xml"
private const val ABOUT_XML = "res/xml/setting_about.xml"

private const val ZHUYIN_TOP_TEMPLATE_ID = "@id/id_0x7f0b21d6"
private const val ZHUYIN_BOTTOM_TEMPLATE_ID = "@id/id_0x7f0b21d7"
private const val ENGLISH_QWERTY_TEMPLATE_ID = "@id/id_0x7f0b21c5"
private const val FOOTER_LABEL_LOCATION = "@id/id_0x7f0b060f"
private const val ENGLISH_QWERTY_LAYOUT = "@layout/layout_0x7f0e0734"
private const val ABOUT_VERSION_KEY_REF = "@string/string_0x7f140b7d"
private const val ABOUT_PREFERENCE_CLASS =
    "com.google.android.libraries.inputmethod.settings.widget.ExtendedPreference"

private val ENGLISH_SLIDE_DOWN_MAP = linkedMapOf(
    "q" to "1",
    "w" to "2",
    "e" to "3",
    "r" to "4",
    "t" to "5",
    "y" to "6",
    "u" to "7",
    "i" to "8",
    "o" to "9",
    "p" to "0",
    "a" to "@",
    "s" to "*",
    "d" to "+",
    "f" to "-",
    "g" to "=",
    "h" to "/",
    "j" to "#",
    "k" to "(",
    "l" to ")",
    "z" to "'",
    "x" to ":",
    "c" to "\"",
    "v" to "?",
    "b" to "!",
    "n" to "~",
    "m" to "…"
)

private val ZHUYIN_BOTTOM_ROW_SLIDE_DOWN_MAP = linkedMapOf(
    "ㄝ" to "…",
    "ㄡ" to "！",
    "ㄤ" to "：",
    "ㄥ" to "？"
)

private val gboardPackageNamePatch = resourcePatch(
    description = "將套件名稱改成可共存安裝的自訂值。"
) {
    finalize {
        applyManifestPackageOverride(
            originalPackageName = GBOARD_PACKAGE_NAME,
            packageNameOverride = GBOARD_PATCHED_PACKAGE_NAME
        )
    }
}

private val gboardZhuyinSlideResourcePatch = resourcePatch(
    description = "補回注音鍵盤的滑動輸入資源 metadata。"
) {
    finalize {
        applyZhuyinResourcePatch()
    }
}

private val gboardEnglishQwertyResourcePatch = resourcePatch(
    description = "補回英文 QWERTY 的滑動輸入資源 metadata。"
) {
    finalize {
        applyEnglishQwertyResourcePatch()
    }
}

private val gboardAboutPagePatch = resourcePatch(
    description = "在關於頁加入 patch 資訊。"
) {
    finalize {
        applyAboutPagePatch()
    }
}

@Suppress("unused")
val gboardZhuyinSlideInputPatch = resourcePatch(
    name = "Zhuyin Slide Input",
    description = "注音鍵盤支持上下滑輸入",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardAboutPagePatch,
        gboardZhuyinSlideResourcePatch,
        gboardPointerAnchorPatch
    )
}

@Suppress("unused")
val gboardEnglishQwertySlideSymbolsPatch = resourcePatch(
    name = "English QWERTY Slide Symbols",
    description = "英文 QWERTY 鍵盤支持上下滑符號輸入",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardAboutPagePatch,
        gboardEnglishQwertyResourcePatch,
        gboardEnglishQwertySoftKeyPatch
    )
}

@Suppress("unused")
val gboardZhuyinQuickTraditionalSimplifiedTogglePatch = resourcePatch(
    name = "Zhuyin Quick Traditional/Simplified Toggle",
    description = "依賴注音上下滑，在注音 ㄥ 上滑快速切換繁簡",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardZhuyinSlideInputPatch,
        gboardAboutPagePatch,
        gboardZhuyinSlideResourcePatch,
        gboardPointerAnchorPatch,
        gboardZhuyinToggleSoftKeyPatch,
        gboardZhuyinToggleRuntimePatch
    )
}

@Suppress("unused")
val gboardPackageRenamePatch = resourcePatch(
    name = "Package Rename",
    description = "將套件名稱改成 com.google.android.inputmethod.latin.jason.dev 以便共存安裝",
    default = true
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(
        gboardPackageNamePatch
    )
}

context(ResourcePatchContext)
private fun applyManifestPackageOverride(
    originalPackageName: String,
    packageNameOverride: String
) {
    document("AndroidManifest.xml").use { document ->
        val manifest = document.documentElement
        if (manifest.getAttribute("package") == originalPackageName) {
            manifest.setAttribute("package", packageNameOverride)
        }

        document.getElementsByTagName("*").elements().forEach { element ->
            val attributes = element.attributes
            for (index in 0 until attributes.length) {
                val attribute = attributes.item(index)
                if (element === manifest && attribute.nodeName == "package") continue

                val updatedValue = attribute.nodeValue?.replace(originalPackageName, packageNameOverride)
                if (updatedValue != null && updatedValue != attribute.nodeValue) {
                    attribute.nodeValue = updatedValue
                }
            }
        }
    }
}

context(ResourcePatchContext)
private fun applyZhuyinResourcePatch() {
    document(ZHUYIN_TEMPLATE_XML).use { templateDocument ->
        val topTemplate = templateDocument.findSoftkeyTemplate(ZHUYIN_TOP_TEMPLATE_ID)
        ensureTemplateAction(
            topTemplate,
            type = "SLIDE_UP",
            data = "\$slideup_data\$",
            popupLabel = "\$slideup_data\$"
        )
        ensureTemplateAction(
            topTemplate,
            type = "SLIDE_DOWN",
            data = "\$press_data\$",
            popupLabel = "\$press_data\$"
        )
        setFooterLabelValue(topTemplate, FOOTER_LABEL_LOCATION, "\$press_data\$")

        val bottomTemplate = templateDocument.findSoftkeyTemplate(ZHUYIN_BOTTOM_TEMPLATE_ID)
        ensureTemplateAction(
            bottomTemplate,
            type = "SLIDE_DOWN",
            data = "\$slidedown_data\$",
            popupLabel = "\$slidedown_data\$"
        )
        setFooterLabelValue(bottomTemplate, FOOTER_LABEL_LOCATION, "\$slidedown_data\$")
    }

    document(ZHUYIN_KEYS_XML).use { keysDocument ->
        keysDocument.findSoftkeyList(ZHUYIN_TOP_TEMPLATE_ID)
            .childElements("softkey")
            .forEach { key ->
                if (key.hasAttribute("slideup_data")) return@forEach

                splitNonBlankTokens(key.getAttribute("long_press_data"))
                    .getOrNull(2)
                    ?.let { slideUpValue ->
                        key.setAttribute("slideup_data", slideUpValue)
                    }
            }

        keysDocument.findSoftkeyList(ZHUYIN_BOTTOM_TEMPLATE_ID)
            .childElements("softkey")
            .forEach { key ->
                when (val pressLabel = key.getAttribute("press_label")) {
                    "ㄦ" -> key.removeAttribute("slidedown_data")
                    in ZHUYIN_BOTTOM_ROW_SLIDE_DOWN_MAP ->
                        key.setAttribute("slidedown_data", ZHUYIN_BOTTOM_ROW_SLIDE_DOWN_MAP.getValue(pressLabel))
                }
            }
    }
}

context(ResourcePatchContext)
private fun applyEnglishQwertyResourcePatch() {
    document(ENGLISH_QWERTY_XML).use { document ->
        val template = document.findSoftkeyTemplate(ENGLISH_QWERTY_TEMPLATE_ID)
        template.setAttribute("layout", ENGLISH_QWERTY_LAYOUT)
        ensureTemplateAction(
            template,
            type = "SLIDE_UP",
            data = "\$slideup_data\$",
            popupLabel = "\$slideup_data\$"
        )
        ensureTemplateAction(
            template,
            type = "SLIDE_DOWN",
            data = "\$slidedown_data\$",
            popupLabel = "\$slidedown_data\$"
        )
        setFooterLabelValue(template, FOOTER_LABEL_LOCATION, "\$slidedown_data\$")

        document.findSoftkeyList(ENGLISH_QWERTY_TEMPLATE_ID)
            .childElements("softkey")
            .forEach { key ->
                val pressData = key.getAttribute("press_data")
                resolveEnglishSlideUpValue(pressData)?.let { key.setAttribute("slideup_data", it) }
                resolveEnglishSlideDownValue(pressData)?.let { key.setAttribute("slidedown_data", it) }
            }
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

private fun Document.findSoftkeyTemplate(id: String): Element =
    getElementsByTagName("softkey_template").elements().firstOrNull {
        it.getAttribute("id") == id
    } ?: error("Could not find softkey_template with id $id")

private fun Document.findSoftkeyList(templateId: String): Element =
    getElementsByTagName("softkey_list").elements().firstOrNull {
        it.getAttribute("template_id") == templateId
    } ?: error("Could not find softkey_list with template_id $templateId")

private fun ensureTemplateAction(
    templateElement: Element,
    type: String,
    data: String,
    popupLabel: String
) {
    val action = templateElement.childElements("action").firstOrNull {
        it.getAttribute("type") == type
    } ?: templateElement.ownerDocument.createElement("action").also { createdAction ->
        val longPressAction = templateElement.childElements("action").firstOrNull {
            it.getAttribute("type") == "LONG_PRESS"
        }
        if (longPressAction != null) {
            templateElement.insertBefore(createdAction, longPressAction)
        } else {
            templateElement.appendChild(createdAction)
        }
    }

    action.setAttribute("type", type)
    action.setAttribute("data", data)
    action.setAttribute("intention", "COMMIT")
    action.setAttribute("keycode", "PLAIN_TEXT")
    action.setAttribute("popup_label", popupLabel)
}

private fun setFooterLabelValue(templateElement: Element, location: String, value: String) {
    val footerLabel = templateElement.childElements("label").firstOrNull {
        it.getAttribute("location") == location
    } ?: templateElement.ownerDocument.createElement("label").also { label ->
        templateElement.appendChild(label)
    }

    footerLabel.setAttribute("location", location)
    footerLabel.setAttribute("value", value)
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

private fun Node.insertAfter(newNode: Node, referenceNode: Node) {
    val parent = if (this.nodeType == Node.DOCUMENT_NODE) {
        (this as Document).documentElement
    } else {
        this
    }
    val nextSibling = referenceNode.nextSibling
    if (nextSibling == null) {
        parent.appendChild(newNode)
    } else {
        parent.insertBefore(newNode, nextSibling)
    }
}

private fun NodeList.elements(): Sequence<Element> =
    (0 until length).asSequence().mapNotNull { item(it) as? Element }

private fun Element.childElements(tagName: String? = null): Sequence<Element> =
    childNodes.elements().filter { tagName == null || it.tagName == tagName }

private fun splitNonBlankTokens(text: String): List<String> =
    text.split(Regex("\\s+")).filter { it.isNotBlank() }

private fun resolveEnglishSlideUpValue(pressData: String): String? {
    if (pressData.length != 1) return null

    val char = pressData.single()
    return when {
        char in 'a'..'z' -> char.uppercaseChar().toString()
        char in 'A'..'Z' -> char.lowercaseChar().toString()
        else -> null
    }
}

private fun resolveEnglishSlideDownValue(pressData: String): String? =
    ENGLISH_SLIDE_DOWN_MAP[pressData.lowercase()]
