package fr.pokenity.pokenity.presentation.settings

import fr.pokenity.pokenity.domain.model.LanguageOption

data class SettingsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val selectedLanguageCode: String = "en",
    val languages: List<LanguageOption> = emptyList()
)
