package fr.pokenity.pokenity.presentation.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.pokenity.core.AppLanguageState
import fr.pokenity.pokenity.data.remote.PokeApiService
import fr.pokenity.pokenity.data.repository.PokemonRepositoryImpl
import fr.pokenity.pokenity.domain.model.PokemonFilterOption
import fr.pokenity.pokenity.domain.model.PokemonSummary
import fr.pokenity.pokenity.domain.usecase.GetPokemonAbilitiesUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonByAbilityUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonByGenerationUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonByHabitatUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonByRegionUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonByShapeUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonByTypeUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonGenerationsUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonHabitatsUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonListUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonRegionsUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonShapesUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonTypesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class PokedexViewModel(
    private val getPokemonListUseCase: GetPokemonListUseCase,
    private val getPokemonTypesUseCase: GetPokemonTypesUseCase,
    private val getPokemonGenerationsUseCase: GetPokemonGenerationsUseCase,
    private val getPokemonAbilitiesUseCase: GetPokemonAbilitiesUseCase,
    private val getPokemonHabitatsUseCase: GetPokemonHabitatsUseCase,
    private val getPokemonRegionsUseCase: GetPokemonRegionsUseCase,
    private val getPokemonShapesUseCase: GetPokemonShapesUseCase,
    private val getPokemonByTypeUseCase: GetPokemonByTypeUseCase,
    private val getPokemonByGenerationUseCase: GetPokemonByGenerationUseCase,
    private val getPokemonByAbilityUseCase: GetPokemonByAbilityUseCase,
    private val getPokemonByHabitatUseCase: GetPokemonByHabitatUseCase,
    private val getPokemonByRegionUseCase: GetPokemonByRegionUseCase,
    private val getPokemonByShapeUseCase: GetPokemonByShapeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokedexUiState())
    val uiState: StateFlow<PokedexUiState> = _uiState.asStateFlow()

    init {
        loadPokedexData()
        observeLanguageChanges()
    }

    fun loadPokedexData() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val pokemonDeferred = async { getPokemonListUseCase(limit = 120) }
                val typesDeferred = async { getPokemonTypesUseCase() }
                val generationsDeferred = async { getPokemonGenerationsUseCase() }
                val abilitiesDeferred = async { getPokemonAbilitiesUseCase() }
                val habitatsDeferred = async { getPokemonHabitatsUseCase() }
                val regionsDeferred = async { getPokemonRegionsUseCase() }
                val shapesDeferred = async { getPokemonShapesUseCase() }

                PokedexDataBundle(
                    pokemon = pokemonDeferred.await(),
                    types = typesDeferred.await(),
                    generations = generationsDeferred.await(),
                    abilities = abilitiesDeferred.await(),
                    habitats = habitatsDeferred.await(),
                    regions = regionsDeferred.await(),
                    shapes = shapesDeferred.await()
                )
            }.onSuccess { data ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pokemon = data.pokemon,
                    types = data.types,
                    generations = data.generations,
                    abilities = data.abilities,
                    habitats = data.habitats,
                    regions = data.regions,
                    shapes = data.shapes
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
            selectedAbilityLabel = null,
            selectedHabitatLabel = null,
            selectedRegionLabel = null,
            selectedShapeLabel = null,
            filteredPokemon = emptyList(),
            errorMessage = null
        )
    }

    fun onTypeClicked(type: PokemonFilterOption) {
        loadFilteredPokemon(
            selectedLabel = type.label,
            clearOtherSelections = {
                copy(
                    selectedGenerationLabel = null,
                    selectedAbilityLabel = null,
                    selectedHabitatLabel = null,
                    selectedRegionLabel = null,
                    selectedShapeLabel = null
                )
            },
            loader = { getPokemonByTypeUseCase(type.apiName) },
            error = "Impossible de charger les Pokemon de ce type.",
            labelSetter = { state, label -> state.copy(selectedTypeLabel = label) }
        )
    }

    fun onGenerationClicked(generation: PokemonFilterOption) {
        loadFilteredPokemon(
            selectedLabel = generation.label,
            clearOtherSelections = {
                copy(
                    selectedTypeLabel = null,
                    selectedAbilityLabel = null,
                    selectedHabitatLabel = null,
                    selectedRegionLabel = null,
                    selectedShapeLabel = null
                )
            },
            loader = { getPokemonByGenerationUseCase(generation.apiName) },
            error = "Impossible de charger les Pokemon de cette generation.",
            labelSetter = { state, label -> state.copy(selectedGenerationLabel = label) }
        )
    }

    fun onAbilityClicked(ability: PokemonFilterOption) {
        loadFilteredPokemon(
            selectedLabel = ability.label,
            clearOtherSelections = {
                copy(
                    selectedTypeLabel = null,
                    selectedGenerationLabel = null,
                    selectedHabitatLabel = null,
                    selectedRegionLabel = null,
                    selectedShapeLabel = null
                )
            },
            loader = { getPokemonByAbilityUseCase(ability.apiName) },
            error = "Impossible de charger les Pokemon de cette capacite.",
            labelSetter = { state, label -> state.copy(selectedAbilityLabel = label) }
        )
    }

    fun onHabitatClicked(habitat: PokemonFilterOption) {
        loadFilteredPokemon(
            selectedLabel = habitat.label,
            clearOtherSelections = {
                copy(
                    selectedTypeLabel = null,
                    selectedGenerationLabel = null,
                    selectedAbilityLabel = null,
                    selectedRegionLabel = null,
                    selectedShapeLabel = null
                )
            },
            loader = { getPokemonByHabitatUseCase(habitat.apiName) },
            error = "Impossible de charger les Pokemon de cet habitat.",
            labelSetter = { state, label -> state.copy(selectedHabitatLabel = label) }
        )
    }

    fun onRegionClicked(region: PokemonFilterOption) {
        loadFilteredPokemon(
            selectedLabel = region.label,
            clearOtherSelections = {
                copy(
                    selectedTypeLabel = null,
                    selectedGenerationLabel = null,
                    selectedAbilityLabel = null,
                    selectedHabitatLabel = null,
                    selectedShapeLabel = null
                )
            },
            loader = { getPokemonByRegionUseCase(region.apiName) },
            error = "Impossible de charger les Pokemon de cette region.",
            labelSetter = { state, label -> state.copy(selectedRegionLabel = label) }
        )
    }

    fun onShapeClicked(shape: PokemonFilterOption) {
        loadFilteredPokemon(
            selectedLabel = shape.label,
            clearOtherSelections = {
                copy(
                    selectedTypeLabel = null,
                    selectedGenerationLabel = null,
                    selectedAbilityLabel = null,
                    selectedHabitatLabel = null,
                    selectedRegionLabel = null
                )
            },
            loader = { getPokemonByShapeUseCase(shape.apiName) },
            error = "Impossible de charger les Pokemon de cette forme.",
            labelSetter = { state, label -> state.copy(selectedShapeLabel = label) }
        )
    }

    fun clearTypeFilter() {
        _uiState.value = _uiState.value.copy(selectedTypeLabel = null, filteredPokemon = emptyList(), errorMessage = null)
    }

    fun clearGenerationFilter() {
        _uiState.value = _uiState.value.copy(selectedGenerationLabel = null, filteredPokemon = emptyList(), errorMessage = null)
    }

    fun clearAbilityFilter() {
        _uiState.value = _uiState.value.copy(selectedAbilityLabel = null, filteredPokemon = emptyList(), errorMessage = null)
    }

    fun clearHabitatFilter() {
        _uiState.value = _uiState.value.copy(selectedHabitatLabel = null, filteredPokemon = emptyList(), errorMessage = null)
    }

    fun clearRegionFilter() {
        _uiState.value = _uiState.value.copy(selectedRegionLabel = null, filteredPokemon = emptyList(), errorMessage = null)
    }

    fun clearShapeFilter() {
        _uiState.value = _uiState.value.copy(selectedShapeLabel = null, filteredPokemon = emptyList(), errorMessage = null)
    }

    private fun loadFilteredPokemon(
        selectedLabel: String,
        clearOtherSelections: PokedexUiState.() -> PokedexUiState,
        loader: suspend () -> List<PokemonSummary>,
        error: String,
        labelSetter: (PokedexUiState, String) -> PokedexUiState
    ) {
        var nextState = _uiState.value.copy(isLoading = true, errorMessage = null)
        nextState = nextState.clearOtherSelections()
        nextState = labelSetter(nextState, selectedLabel)
        _uiState.value = nextState

        viewModelScope.launch(Dispatchers.IO) {
            runCatching { loader() }
                .onSuccess { pokemon ->
                    _uiState.value = _uiState.value.copy(isLoading = false, filteredPokemon = pokemon)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error)
                }
        }
    }

    private fun observeLanguageChanges() {
        viewModelScope.launch {
            AppLanguageState.selectedLanguageCode.drop(1).collect {
                loadPokedexData()
            }
        }
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
                val abilitiesUseCase = GetPokemonAbilitiesUseCase(repository)
                val habitatsUseCase = GetPokemonHabitatsUseCase(repository)
                val regionsUseCase = GetPokemonRegionsUseCase(repository)
                val shapesUseCase = GetPokemonShapesUseCase(repository)
                val pokemonByTypeUseCase = GetPokemonByTypeUseCase(repository)
                val pokemonByGenerationUseCase = GetPokemonByGenerationUseCase(repository)
                val pokemonByAbilityUseCase = GetPokemonByAbilityUseCase(repository)
                val pokemonByHabitatUseCase = GetPokemonByHabitatUseCase(repository)
                val pokemonByRegionUseCase = GetPokemonByRegionUseCase(repository)
                val pokemonByShapeUseCase = GetPokemonByShapeUseCase(repository)
                return PokedexViewModel(
                    pokemonUseCase,
                    typesUseCase,
                    generationsUseCase,
                    abilitiesUseCase,
                    habitatsUseCase,
                    regionsUseCase,
                    shapesUseCase,
                    pokemonByTypeUseCase,
                    pokemonByGenerationUseCase,
                    pokemonByAbilityUseCase,
                    pokemonByHabitatUseCase,
                    pokemonByRegionUseCase,
                    pokemonByShapeUseCase
                ) as T
            }
        }
    }
}

private data class PokedexDataBundle(
    val pokemon: List<PokemonSummary>,
    val types: List<PokemonFilterOption>,
    val generations: List<PokemonFilterOption>,
    val abilities: List<PokemonFilterOption>,
    val habitats: List<PokemonFilterOption>,
    val regions: List<PokemonFilterOption>,
    val shapes: List<PokemonFilterOption>
)
