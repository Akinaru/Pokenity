package fr.pokenity.pokenity.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppLanguageState {
    private val _selectedLanguageCode = MutableStateFlow("en")
    val selectedLanguageCode: StateFlow<String> = _selectedLanguageCode.asStateFlow()

    fun setLanguage(code: String) {
        _selectedLanguageCode.value = code
    }

    fun currentLanguage(): String = _selectedLanguageCode.value
}
