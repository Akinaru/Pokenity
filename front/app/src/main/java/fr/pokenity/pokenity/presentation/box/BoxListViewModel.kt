package fr.pokenity.pokenity.presentation.box

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.data.core.AppContainer
import fr.pokenity.pokenity.domain.usecase.GetBoxesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BoxListViewModel(
    private val getBoxesUseCase: GetBoxesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoxListUiState())
    val uiState: StateFlow<BoxListUiState> = _uiState.asStateFlow()

    init {
        loadBoxes()
    }

    fun loadBoxes() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getBoxesUseCase() }
                .onSuccess { boxes ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        boxes = boxes
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Impossible de charger les boxes.",
                        boxes = emptyList()
                    )
                }
        }
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BoxListViewModel(
                    getBoxesUseCase = GetBoxesUseCase(AppContainer.boxRepository)
                ) as T
            }
        }
    }
}
