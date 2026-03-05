package fr.pokenity.pokenity.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.data.core.AppContainer
import fr.pokenity.data.model.AuthenticatedUser
import fr.pokenity.pokenity.domain.usecase.AuthFetchCurrentUserUseCase
import fr.pokenity.pokenity.domain.usecase.AuthLogoutUseCase
import fr.pokenity.pokenity.domain.usecase.AuthObserveTokenUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AccountViewModel(
    private val authFetchCurrentUserUseCase: AuthFetchCurrentUserUseCase,
    private val authObserveTokenUseCase: AuthObserveTokenUseCase,
    private val authLogoutUseCase: AuthLogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authObserveTokenUseCase().collectLatest { token ->
                _uiState.value = _uiState.value.copy(token = token)
                if (token == null) {
                    _uiState.value = _uiState.value.copy(
                        user = null,
                        isLoading = false,
                        errorMessage = null,
                        infoMessage = null
                    )
                } else if (_uiState.value.user == null) {
                    fetchMe()
                }
            }
        }
    }

    fun fetchMe() {
        if (_uiState.value.token == null) {
            _uiState.value = _uiState.value.copy(user = null, isLoading = false)
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { authFetchCurrentUserUseCase() }
                .onSuccess { me ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = me.toUi()
                    )
                }
                .onFailure {
                    authLogoutUseCase()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        user = null,
                        errorMessage = it.message ?: "Session invalide"
                    )
                }
        }
    }

    fun logout() {
        authLogoutUseCase()
        _uiState.value = _uiState.value.copy(
            user = null,
            token = null,
            infoMessage = "Deconnecte"
        )
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = AppContainer.authRepository
                return AccountViewModel(
                    authFetchCurrentUserUseCase = AuthFetchCurrentUserUseCase(repository),
                    authObserveTokenUseCase = AuthObserveTokenUseCase(repository),
                    authLogoutUseCase = AuthLogoutUseCase(repository)
                ) as T
            }
        }
    }
}

private fun AuthenticatedUser.toUi(): AccountUser {
    return AccountUser(
        id = id,
        username = username,
        email = email,
        createdAt = createdAt,
        characterName = character?.name,
        characterAvatarUrl = character?.avatarUrl,
        characterImageUrl = character?.imageUrl
    )
}
