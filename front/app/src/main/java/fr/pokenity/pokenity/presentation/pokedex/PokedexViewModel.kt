package fr.pokenity.pokenity.presentation.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.pokenity.data.remote.PokeApiService
import fr.pokenity.pokenity.data.repository.PokemonRepositoryImpl
import fr.pokenity.pokenity.domain.usecase.GetPokemonGenerationsUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonListUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonTypesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PokedexViewModel(
    private val getPokemonListUseCase: GetPokemonListUseCase,
    private val getPokemonTypesUseCase: GetPokemonTypesUseCase,
    private val getPokemonGenerationsUseCase: GetPokemonGenerationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokedexUiState())
    val uiState: StateFlow<PokedexUiState> = _uiState.asStateFlow()

    init {
        loadPokedexData()
    }

    fun loadPokedexData() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val pokemonDeferred = async { getPokemonListUseCase(limit = 120) }
                val typesDeferred = async { getPokemonTypesUseCase() }
                val generationsDeferred = async { getPokemonGenerationsUseCase() }

                Triple(
                    pokemonDeferred.await(),
                    typesDeferred.await(),
                    generationsDeferred.await()
                )
            }.onSuccess { (pokemon, types, generations) ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pokemon = pokemon,
                    types = types,
                    generations = generations
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Impossible de charger les donnees. Verifie ta connexion puis reessaie."
                )
            }
        }
    }

    fun onSectionSelected(section: PokedexSection) {
        _uiState.value = _uiState.value.copy(selectedSection = section)
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val pokeApiService = PokeApiService()
                val repository = PokemonRepositoryImpl(pokeApiService)
                val pokemonUseCase = GetPokemonListUseCase(repository)
                val typesUseCase = GetPokemonTypesUseCase(repository)
                val generationsUseCase = GetPokemonGenerationsUseCase(repository)
                return PokedexViewModel(pokemonUseCase, typesUseCase, generationsUseCase) as T
            }
        }
    }
}
