package fr.pokenity.pokenity.presentation.pokedex

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.pokenity.core.AppContainer
import fr.pokenity.pokenity.core.AppLanguageState
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
import kotlinx.coroutines.awaitAll
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
    private val pageSize = 30
    private var currentOffset = 0

    private val _uiState = MutableStateFlow(PokedexUiState())
    val uiState: StateFlow<PokedexUiState> = _uiState.asStateFlow()

    init {
        loadPokedexData()
        observeLanguageChanges()
    }

    fun loadPokedexData() {
        currentOffset = 0
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            isLoadingMore = false,
            hasMorePokemon = true,
            pokemon = emptyList(),
            filteredPokemon = emptyList(),
            selectedType = null,
            selectedGeneration = null,
            selectedAbility = null,
            selectedHabitat = null,
            selectedRegion = null,
            selectedShape = null,
            errorMessage = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getPokemonListUseCase(limit = pageSize, offset = 0) }
                .onSuccess { pokemon ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pokemon = pokemon,
                        hasMorePokemon = pokemon.size == pageSize
                    )
                    currentOffset = pokemon.size
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Impossible de charger les Pokemon."
                    )
                }
        }
    }

    fun loadMorePokemonIfNeeded() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMorePokemon) return
        if (hasActiveFilters(state)) return

        _uiState.value = state.copy(isLoadingMore = true)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getPokemonListUseCase(limit = pageSize, offset = currentOffset) }
                .onSuccess { nextPage ->
                    val updated = _uiState.value.pokemon + nextPage
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        pokemon = updated,
                        hasMorePokemon = nextPage.size == pageSize
                    )
                    currentOffset += nextPage.size
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        errorMessage = "Impossible de charger plus de Pokemon."
                    )
                }
        }
    }

    fun onFilterCategorySelected(category: PokedexSection) {
        ensureOptionsLoaded(category)
    }

    fun onTypeClicked(type: PokemonFilterOption) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        applyCombinedFilters()
    }

    fun onGenerationClicked(generation: PokemonFilterOption) {
        _uiState.value = _uiState.value.copy(selectedGeneration = generation)
        applyCombinedFilters()
    }

    fun onAbilityClicked(ability: PokemonFilterOption) {
        _uiState.value = _uiState.value.copy(selectedAbility = ability)
        applyCombinedFilters()
    }

    fun onHabitatClicked(habitat: PokemonFilterOption) {
        _uiState.value = _uiState.value.copy(selectedHabitat = habitat)
        applyCombinedFilters()
    }

    fun onRegionClicked(region: PokemonFilterOption) {
        _uiState.value = _uiState.value.copy(selectedRegion = region)
        applyCombinedFilters()
    }

    fun onShapeClicked(shape: PokemonFilterOption) {
        _uiState.value = _uiState.value.copy(selectedShape = shape)
        applyCombinedFilters()
    }

    fun clearTypeFilter() {
        _uiState.value = _uiState.value.copy(selectedType = null)
        applyCombinedFilters()
    }

    fun clearGenerationFilter() {
        _uiState.value = _uiState.value.copy(selectedGeneration = null)
        applyCombinedFilters()
    }

    fun clearAbilityFilter() {
        _uiState.value = _uiState.value.copy(selectedAbility = null)
        applyCombinedFilters()
    }

    fun clearHabitatFilter() {
        _uiState.value = _uiState.value.copy(selectedHabitat = null)
        applyCombinedFilters()
    }

    fun clearRegionFilter() {
        _uiState.value = _uiState.value.copy(selectedRegion = null)
        applyCombinedFilters()
    }

    fun clearShapeFilter() {
        _uiState.value = _uiState.value.copy(selectedShape = null)
        applyCombinedFilters()
    }

    private fun applyCombinedFilters() {
        val state = _uiState.value
        if (!hasActiveFilters(state)) {
            _uiState.value = state.copy(filteredPokemon = emptyList(), errorMessage = null)
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val selectedType = _uiState.value.selectedType
                val selectedGeneration = _uiState.value.selectedGeneration
                val selectedAbility = _uiState.value.selectedAbility
                val selectedHabitat = _uiState.value.selectedHabitat
                val selectedRegion = _uiState.value.selectedRegion
                val selectedShape = _uiState.value.selectedShape

                val deferredLists = buildList {
                    selectedType?.let { add(async { getPokemonByTypeUseCase(it.apiName) }) }
                    selectedGeneration?.let { add(async { getPokemonByGenerationUseCase(it.apiName) }) }
                    selectedAbility?.let { add(async { getPokemonByAbilityUseCase(it.apiName) }) }
                    selectedHabitat?.let { add(async { getPokemonByHabitatUseCase(it.apiName) }) }
                    selectedRegion?.let { add(async { getPokemonByRegionUseCase(it.apiName) }) }
                    selectedShape?.let { add(async { getPokemonByShapeUseCase(it.apiName) }) }
                }

                val lists = deferredLists.awaitAll()
                intersectPokemonLists(lists)
            }.onSuccess { filtered ->
                _uiState.value = _uiState.value.copy(isLoading = false, filteredPokemon = filtered)
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Impossible d'appliquer les filtres."
                )
            }
        }
    }

    private fun intersectPokemonLists(lists: List<List<PokemonSummary>>): List<PokemonSummary> {
        if (lists.isEmpty()) return emptyList()

        val idIntersection = lists
            .map { list -> list.map { it.id }.toSet() }
            .reduce { acc, ids -> acc intersect ids }

        val reference = lists.first().associateBy { it.id }
        return idIntersection.mapNotNull { reference[it] }.sortedBy { it.id }
    }

    private fun hasActiveFilters(state: PokedexUiState): Boolean {
        return state.selectedType != null ||
            state.selectedGeneration != null ||
            state.selectedAbility != null ||
            state.selectedHabitat != null ||
            state.selectedRegion != null ||
            state.selectedShape != null
    }

    private fun ensureOptionsLoaded(section: PokedexSection) {
        when (section) {
            PokedexSection.TYPE -> if (_uiState.value.types.isEmpty()) loadOptions { copy(types = getPokemonTypesUseCase()) }
            PokedexSection.GENERATION -> if (_uiState.value.generations.isEmpty()) loadOptions { copy(generations = getPokemonGenerationsUseCase()) }
            PokedexSection.ABILITY -> if (_uiState.value.abilities.isEmpty()) loadOptions { copy(abilities = getPokemonAbilitiesUseCase()) }
            PokedexSection.HABITAT -> if (_uiState.value.habitats.isEmpty()) loadOptions { copy(habitats = getPokemonHabitatsUseCase()) }
            PokedexSection.REGION -> if (_uiState.value.regions.isEmpty()) loadOptions { copy(regions = getPokemonRegionsUseCase()) }
            PokedexSection.SHAPE -> if (_uiState.value.shapes.isEmpty()) loadOptions { copy(shapes = getPokemonShapesUseCase()) }
        }
    }

    private fun loadOptions(transform: suspend PokedexUiState.() -> PokedexUiState) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { _uiState.value.transform() }
                .onSuccess { next -> _uiState.value = next.copy(isLoading = false) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Impossible de charger cette liste."
                    )
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
                val repository = AppContainer.pokemonRepository
                return PokedexViewModel(
                    getPokemonListUseCase = GetPokemonListUseCase(repository),
                    getPokemonTypesUseCase = GetPokemonTypesUseCase(repository),
                    getPokemonGenerationsUseCase = GetPokemonGenerationsUseCase(repository),
                    getPokemonAbilitiesUseCase = GetPokemonAbilitiesUseCase(repository),
                    getPokemonHabitatsUseCase = GetPokemonHabitatsUseCase(repository),
                    getPokemonRegionsUseCase = GetPokemonRegionsUseCase(repository),
                    getPokemonShapesUseCase = GetPokemonShapesUseCase(repository),
                    getPokemonByTypeUseCase = GetPokemonByTypeUseCase(repository),
                    getPokemonByGenerationUseCase = GetPokemonByGenerationUseCase(repository),
                    getPokemonByAbilityUseCase = GetPokemonByAbilityUseCase(repository),
                    getPokemonByHabitatUseCase = GetPokemonByHabitatUseCase(repository),
                    getPokemonByRegionUseCase = GetPokemonByRegionUseCase(repository),
                    getPokemonByShapeUseCase = GetPokemonByShapeUseCase(repository)
                ) as T
            }
        }
    }
}
