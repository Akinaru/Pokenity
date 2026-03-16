package fr.pokenity.pokenity.presentation.settings

import fr.pokenity.data.core.PokemonImageType
import fr.pokenity.data.core.AppThemeMode
import fr.pokenity.data.core.AppUiLanguage
import fr.pokenity.data.model.LanguageOption

data class SettingsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val selectedLanguageCode: String = "en",
    val selectedUiLanguage: AppUiLanguage = AppUiLanguage.FRENCH,
    val selectedImageType: PokemonImageType = PokemonImageType.OFFICIAL_ARTWORK,
    val selectedThemeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val languages: List<LanguageOption> = emptyList()
)
