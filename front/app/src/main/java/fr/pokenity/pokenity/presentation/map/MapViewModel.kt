package fr.pokenity.pokenity.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.pokenity.core.AppLanguageState
import fr.pokenity.pokenity.core.AppContainer
import fr.pokenity.pokenity.domain.model.PokemonFilterOption
import fr.pokenity.pokenity.domain.usecase.GetLocationAreasByLocationUseCase
import fr.pokenity.pokenity.domain.usecase.GetLocationsByRegionUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonByLocationAreaUseCase
import fr.pokenity.pokenity.domain.usecase.GetPokemonRegionsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class MapViewModel(
    private val getPokemonRegionsUseCase: GetPokemonRegionsUseCase,
    private val getLocationsByRegionUseCase: GetLocationsByRegionUseCase,
    private val getLocationAreasByLocationUseCase: GetLocationAreasByLocationUseCase,
    private val getPokemonByLocationAreaUseCase: GetPokemonByLocationAreaUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadRegions()
        observeLanguageChanges()
    }

    fun loadRegions() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getPokemonRegionsUseCase() }
                .onSuccess { regions ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        regions = regions,
                        selectedRegion = null,
                        selectedLocation = null,
                        selectedArea = null,
                        locations = emptyList(),
                        areas = emptyList(),
                        pokemons = emptyList()
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Impossible de charger les regions."
                    )
                }
        }
    }

    fun onRegionSelected(region: PokemonFilterOption) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            selectedRegion = region,
            selectedLocation = null,
            selectedArea = null,
            areas = emptyList(),
            pokemons = emptyList()
        )

        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getLocationsByRegionUseCase(region.apiName) }
                .onSuccess { locations ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        locations = locations
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Impossible de charger les locations de cette region."
                    )
                }
        }
    }

    fun onLocationSelected(location: PokemonFilterOption) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            selectedLocation = location,
            selectedArea = null,
            pokemons = emptyList()
        )

        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getLocationAreasByLocationUseCase(location.apiName) }
                .onSuccess { areas ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        areas = areas
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Impossible de charger les zones de cette location."
                    )
                }
        }
    }

    fun onAreaSelected(area: PokemonFilterOption) {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            selectedArea = area
        )

        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getPokemonByLocationAreaUseCase(area.apiName) }
                .onSuccess { pokemons ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pokemons = pokemons
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Impossible de charger les Pokemon de cette zone."
                    )
                }
        }
    }

    fun backToRegions() {
        _uiState.value = _uiState.value.copy(
            selectedRegion = null,
            selectedLocation = null,
            selectedArea = null,
            locations = emptyList(),
            areas = emptyList(),
            pokemons = emptyList(),
            errorMessage = null
        )
    }

    fun backToLocations() {
        _uiState.value = _uiState.value.copy(
            selectedLocation = null,
            selectedArea = null,
            areas = emptyList(),
            pokemons = emptyList(),
            errorMessage = null
        )
    }

    fun backToAreas() {
        _uiState.value = _uiState.value.copy(
            selectedArea = null,
            pokemons = emptyList(),
            errorMessage = null
        )
    }

    private fun observeLanguageChanges() {
        viewModelScope.launch {
            AppLanguageState.selectedLanguageCode.drop(1).collect {
                loadRegions()
            }
        }
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = AppContainer.pokemonRepository
                return MapViewModel(
                    getPokemonRegionsUseCase = GetPokemonRegionsUseCase(repository),
                    getLocationsByRegionUseCase = GetLocationsByRegionUseCase(repository),
                    getLocationAreasByLocationUseCase = GetLocationAreasByLocationUseCase(repository),
                    getPokemonByLocationAreaUseCase = GetPokemonByLocationAreaUseCase(repository)
                ) as T
            }
        }
    }
}
