package fr.pokenity.pokenity.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.pokenity.data.remote.PokeApiService
import fr.pokenity.pokenity.data.repository.PokemonRepositoryImpl
import fr.pokenity.pokenity.domain.usecase.GetPokemonDetailUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PokemonDetailViewModel(
    private val getPokemonDetailUseCase: GetPokemonDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokemonDetailUiState())
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()

    fun loadPokemon(id: Int) {
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

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val pokeApiService = PokeApiService()
                val repository = PokemonRepositoryImpl(pokeApiService)
                val useCase = GetPokemonDetailUseCase(repository)
                return PokemonDetailViewModel(useCase) as T
            }
        }
    }
}
