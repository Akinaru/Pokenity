package fr.pokenity.pokenity.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.data.core.AppLanguageState
import fr.pokenity.data.core.AppContainer
import fr.pokenity.pokenity.domain.usecase.GetPokemonDetailUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class PokemonDetailViewModel(
    private val getPokemonDetailUseCase: GetPokemonDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokemonDetailUiState())
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()
    private var currentPokemonId: Int? = null

    init {
        observeLanguageChanges()
    }

    fun loadPokemon(id: Int) {
        currentPokemonId = id
        _uiState.value = PokemonDetailUiState(isLoading = true)

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                getPokemonDetailUseCase(id)
            }.onSuccess { pokemon ->
                _uiState.value = PokemonDetailUiState(
                    isLoading = false,
                    pokemon = pokemon
                )
            }.onFailure {
                _uiState.value = PokemonDetailUiState(
                    isLoading = false,
                    errorMessage = "Impossible de charger ce Pokemon. Verifie ta connexion puis reessaie."
                )
            }
        }
    }

    private fun observeLanguageChanges() {
        viewModelScope.launch {
            AppLanguageState.selectedLanguageCode.drop(1).collect {
                val id = currentPokemonId ?: return@collect
                loadPokemon(id)
            }
        }
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                val useCase = GetPokemonDetailUseCase(AppContainer.pokemonRepository)
                return PokemonDetailViewModel(useCase) as T
            }
        }
    }
}
