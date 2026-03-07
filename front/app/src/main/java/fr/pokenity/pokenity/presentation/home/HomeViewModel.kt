package fr.pokenity.pokenity.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.data.core.AppContainer
import fr.pokenity.pokenity.domain.usecase.GetLatestBoxesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getLatestBoxesUseCase: GetLatestBoxesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadLatestBoxes()
    }

    fun loadLatestBoxes() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getLatestBoxesUseCase(limit = 3) }
                .onSuccess { boxes ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        latestBoxes = boxes
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Impossible de charger les boxes.",
                        latestBoxes = emptyList()
                    )
                }
        }
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(
                    getLatestBoxesUseCase = GetLatestBoxesUseCase(AppContainer.boxRepository)
                ) as T
            }
        }
    }
}
