package fr.pokenity.pokenity.presentation.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.data.core.AppContainer
import fr.pokenity.data.core.AppLanguageState
import fr.pokenity.pokenity.domain.usecase.GetPokemonDetailUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

class PokemonCompareViewModel(
    private val getPokemonDetailUseCase: GetPokemonDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokemonCompareUiState())
    val uiState: StateFlow<PokemonCompareUiState> = _uiState.asStateFlow()

    private var baseId: Int? = null
    private var comparedId: Int? = null

    init {
        observeLanguageChanges()
    }

    fun load(basePokemonId: Int, comparedPokemonId: Int?) {
        baseId = basePokemonId
        comparedId = comparedPokemonId
        _uiState.value = PokemonCompareUiState(isLoading = true)

        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val baseDeferred = async { getPokemonDetailUseCase(basePokemonId) }
                val comparedDeferred = comparedPokemonId?.let { async { getPokemonDetailUseCase(it) } }
                baseDeferred.await() to comparedDeferred?.await()
            }.onSuccess { (base, compared) ->
                _uiState.value = PokemonCompareUiState(
                    isLoading = false,
                    basePokemon = base,
                    comparedPokemon = compared
                )
            }.onFailure {
                _uiState.value = PokemonCompareUiState(
                    isLoading = false,
                    errorMessage = "Impossible de charger le comparateur."
                )
            }
        }
    }

    private fun observeLanguageChanges() {
        viewModelScope.launch {
            AppLanguageState.selectedLanguageCode.drop(1).collect {
                val currentBase = baseId ?: return@collect
                load(currentBase, comparedId)
            }
        }
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val useCase = GetPokemonDetailUseCase(AppContainer.pokemonRepository)
                return PokemonCompareViewModel(useCase) as T
            }
        }
    }
}

