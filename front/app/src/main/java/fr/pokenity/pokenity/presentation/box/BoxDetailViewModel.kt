package fr.pokenity.pokenity.presentation.box

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.data.core.AppContainer
import fr.pokenity.pokenity.domain.usecase.GetBoxByIdUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BoxDetailViewModel(
    private val getBoxByIdUseCase: GetBoxByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BoxDetailUiState())
    val uiState: StateFlow<BoxDetailUiState> = _uiState.asStateFlow()

    fun loadBox(boxId: String) {
        if (boxId.isBlank()) {
            _uiState.value = BoxDetailUiState(
                isLoading = false,
                errorMessage = "Identifiant box invalide."
            )
            return
        }

        _uiState.value = BoxDetailUiState(isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { getBoxByIdUseCase(boxId) }
                .onSuccess { box ->
                    _uiState.value = BoxDetailUiState(
                        isLoading = false,
                        box = box
                    )
                }
                .onFailure {
                    _uiState.value = BoxDetailUiState(
                        isLoading = false,
                        errorMessage = it.message ?: "Impossible de charger cette box."
                    )
                }
        }
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return BoxDetailViewModel(
                    getBoxByIdUseCase = GetBoxByIdUseCase(AppContainer.boxRepository)
                ) as T
            }
        }
    }
}
