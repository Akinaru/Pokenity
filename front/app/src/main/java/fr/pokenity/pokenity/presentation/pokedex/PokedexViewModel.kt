package fr.pokenity.pokenity.presentation.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.pokenity.data.remote.PokeApiService
import fr.pokenity.pokenity.data.repository.PokemonRepositoryImpl
import fr.pokenity.pokenity.domain.model.PokemonFilterOption
import fr.pokenity.pokenity.domain.usecase.GetPokemonByGenerationUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonByTypeUseCase
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
    private val getPokemonGenerationsUseCase: GetPokemonGenerationsUseCase,
    private val getPokemonByTypeUseCase: GetPokemonByTypeUseCase,
    private val getPokemonByGenerationUseCase: GetPokemonByGenerationUseCase
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
        _uiState.value = _uiState.value.copy(
            selectedSection = section,
            selectedTypeLabel = null,
            selectedGenerationLabel = null,
            filteredPokemon = emptyList(),
            errorMessage = null
        )
    }

    fun onTypeClicked(type: PokemonFilterOption) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            selectedTypeLabel = type.label,
            selectedGenerationLabel = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                getPokemonByTypeUseCase(type.apiName)
            }.onSuccess { pokemon ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    filteredPokemon = pokemon
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Impossible de charger les Pokemon de ce type."
                )
            }
        }
    }

    fun onGenerationClicked(generation: PokemonFilterOption) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            selectedGenerationLabel = generation.label,
            selectedTypeLabel = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                getPokemonByGenerationUseCase(generation.apiName)
            }.onSuccess { pokemon ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    filteredPokemon = pokemon
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Impossible de charger les Pokemon de cette generation."
                )
            }
        }
    }

    fun clearTypeFilter() {
        _uiState.value = _uiState.value.copy(
            selectedTypeLabel = null,
            filteredPokemon = emptyList(),
            errorMessage = null
        )
    }

    fun clearGenerationFilter() {
        _uiState.value = _uiState.value.copy(
            selectedGenerationLabel = null,
            filteredPokemon = emptyList(),
            errorMessage = null
        )
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
                val pokemonByTypeUseCase = GetPokemonByTypeUseCase(repository)
                val pokemonByGenerationUseCase = GetPokemonByGenerationUseCase(repository)
                return PokedexViewModel(
                    pokemonUseCase,
                    typesUseCase,
                    generationsUseCase,
                    pokemonByTypeUseCase,
                    pokemonByGenerationUseCase
                ) as T
            }
        }
    }
}
