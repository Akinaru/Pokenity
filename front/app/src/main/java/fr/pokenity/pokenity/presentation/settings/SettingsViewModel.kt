package fr.pokenity.pokenity.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.pokenity.core.AppLanguageState
import fr.pokenity.pokenity.data.remote.PokeApiService
import fr.pokenity.pokenity.data.repository.PokemonRepositoryImpl
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

    private fun observeLanguage() {
        viewModelScope.launch {
            AppLanguageState.selectedLanguageCode.collectLatest { code ->
                _uiState.value = _uiState.value.copy(selectedLanguageCode = code)
            }
        }
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = PokemonRepositoryImpl(PokeApiService())
                return SettingsViewModel(
                    getAvailableLanguagesUseCase = GetAvailableLanguagesUseCase(repository)
                ) as T
            }
        }
    }
}
