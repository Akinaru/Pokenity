package fr.pokenity.pokenity.ui.media

import android.net.Uri

private const val ASSET_BASE_PATH = "file:///android_asset/characters"

/**
 * Resolves character media to a model usable by Coil.
 * - http(s) and absolute paths are returned as-is
 * - plain filenames are loaded from app assets/characters
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

    return "$ASSET_BASE_PATH/${Uri.encode(value)}"
}
