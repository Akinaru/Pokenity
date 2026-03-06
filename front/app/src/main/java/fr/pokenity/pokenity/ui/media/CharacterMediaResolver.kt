package fr.pokenity.pokenity.ui.media

import android.net.Uri

private const val RES_BASE_PATH = "android.resource://fr.pokenity.pokenity/drawable"

/**
 * Resolves character media to a model usable by Coil.
 * - http(s) and absolute paths are returned as-is
 * - plain filenames are loaded from app res/drawable
 */
fun resolveCharacterMediaModel(rawValue: String?): String? {
    val value = (rawValue ?: "").trim()
    if (value.isBlank()) return null

    if (
        value.startsWith("http://") ||
        value.startsWith("https://") ||
        value.startsWith("/") ||
        value.startsWith("file://") ||
        value.startsWith("content://") ||
        value.startsWith("android.resource://")
    ) {
        return value
    }

    val fileName = value.substringAfterLast('/')
    val resourceName = fileName.substringBeforeLast('.').lowercase()
    return "$RES_BASE_PATH/${Uri.encode(resourceName)}"
}
