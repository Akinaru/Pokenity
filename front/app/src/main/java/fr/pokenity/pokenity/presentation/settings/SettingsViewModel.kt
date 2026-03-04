package fr.pokenity.pokenity.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.pokenity.core.AppLanguageState
import fr.pokenity.pokenity.core.AppContainer
import fr.pokenity.pokenity.core.AppThemeState
import fr.pokenity.pokenity.core.AppThemeMode
import fr.pokenity.pokenity.core.PokemonImageSettings
import fr.pokenity.pokenity.core.PokemonImageType
import fr.pokenity.pokenity.domain.usecase.GetAvailableLanguagesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getAvailableLanguagesUseCase: GetAvailableLanguagesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeLanguage()
        observeImageType()
        observeTheme()
        loadLanguages()
    }

    fun loadLanguages() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getAvailableLanguagesUseCase() }
                .onSuccess { languages ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        languages = languages
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Impossible de charger les langues depuis PokeAPI."
                    )
                }
        }
    }

    fun onLanguageSelected(code: String) {
        AppLanguageState.setLanguage(code)
    }

    fun onImageTypeSelected(type: PokemonImageType) {
        PokemonImageSettings.setImageType(type)
    }

    fun onThemeModeSelected(mode: AppThemeMode) {
        AppThemeState.setThemeMode(mode)
    }

    private fun observeLanguage() {
        viewModelScope.launch {
            AppLanguageState.selectedLanguageCode.collectLatest { code ->
                _uiState.value = _uiState.value.copy(selectedLanguageCode = code)
            }
        }
    }

    private fun observeImageType() {
        viewModelScope.launch {
            PokemonImageSettings.imageType.collectLatest { type ->
                _uiState.value = _uiState.value.copy(selectedImageType = type)
            }
        }
    }

    private fun observeTheme() {
        viewModelScope.launch {
            AppThemeState.themeMode.collectLatest { mode ->
                _uiState.value = _uiState.value.copy(selectedThemeMode = mode)
            }
        }
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    getAvailableLanguagesUseCase = GetAvailableLanguagesUseCase(AppContainer.pokemonRepository)
                ) as T
            }
        }
    }
}
