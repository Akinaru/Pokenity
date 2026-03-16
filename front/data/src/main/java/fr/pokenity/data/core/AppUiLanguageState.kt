package fr.pokenity.data.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppUiLanguage(
    val code: String
) {
    FRENCH("fr"),
    ENGLISH("en");

    companion object {
        fun fromCode(code: String): AppUiLanguage = entries.firstOrNull { it.code == code } ?: FRENCH
    }
}

object AppUiLanguageState {
    private val _selectedLanguage = MutableStateFlow(AppUiLanguage.FRENCH)
    val selectedLanguage: StateFlow<AppUiLanguage> = _selectedLanguage.asStateFlow()

    fun setLanguage(language: AppUiLanguage) {
        _selectedLanguage.value = language
    }
}
