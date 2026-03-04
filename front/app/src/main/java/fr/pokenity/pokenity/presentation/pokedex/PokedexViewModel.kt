package fr.pokenity.pokenity.presentation.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.pokenity.data.remote.PokeApiService
import fr.pokenity.pokenity.data.repository.PokemonRepositoryImpl
import fr.pokenity.pokenity.domain.usecase.GetPokemonListUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PokedexViewModel(
    private val getPokemonListUseCase: GetPokemonListUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokedexUiState())
    val uiState: StateFlow<PokedexUiState> = _uiState.asStateFlow()

    init {
        loadPokemon()
    }

    fun loadPokemon() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                getPokemonListUseCase(limit = 120)
            }.onSuccess { pokemon ->
                _uiState.value = PokedexUiState(
                    isLoading = false,
                    pokemon = pokemon
                )
            }.onFailure {
                _uiState.value = PokedexUiState(
                    isLoading = false,
                    errorMessage = "Impossible de charger la liste. Vérifie ta connexion puis réessaie."
                )
            }
        }
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val pokeApiService = PokeApiService()
                val repository = PokemonRepositoryImpl(pokeApiService)
                val useCase = GetPokemonListUseCase(repository)
                return PokedexViewModel(useCase) as T
            }
        }
    }
}
