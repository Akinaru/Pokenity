package fr.pokenity.pokenity.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.pokenity.core.AppContainer
import fr.pokenity.pokenity.domain.model.AuthenticatedUser
import fr.pokenity.pokenity.domain.usecase.AuthFetchCurrentUserUseCase
import fr.pokenity.pokenity.domain.usecase.AuthLoginUseCase
import fr.pokenity.pokenity.domain.usecase.AuthLogoutUseCase
import fr.pokenity.pokenity.domain.usecase.AuthObserveTokenUseCase
import fr.pokenity.pokenity.domain.usecase.AuthRegisterUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AccountViewModel(
    private val authLoginUseCase: AuthLoginUseCase,
    private val authRegisterUseCase: AuthRegisterUseCase,
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
                if (token != null && _uiState.value.user == null) {
                    fetchMe()
                }
            }
        }
    }

    fun setMode(mode: AuthMode) {
        _uiState.value = _uiState.value.copy(mode = mode, errorMessage = null, infoMessage = null)
    }

    fun updateLoginIdentifier(value: String) {
        _uiState.value = _uiState.value.copy(loginIdentifier = value)
    }

    fun updateLoginPassword(value: String) {
        _uiState.value = _uiState.value.copy(loginPassword = value)
    }

    fun updateRegisterUsername(value: String) {
        _uiState.value = _uiState.value.copy(registerUsername = value)
    }

    fun updateRegisterEmail(value: String) {
        _uiState.value = _uiState.value.copy(registerEmail = value)
    }

    fun updateRegisterPassword(value: String) {
        _uiState.value = _uiState.value.copy(registerPassword = value)
    }

    fun login() {
        val state = _uiState.value
        if (state.loginIdentifier.isBlank() || state.loginPassword.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Identifier et mot de passe requis")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null, infoMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                authLoginUseCase(
                    identifier = state.loginIdentifier.trim(),
                    password = state.loginPassword
                )
            }.onSuccess { session ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = session.user.toUi(),
                    loginPassword = "",
                    infoMessage = "Connexion reussie"
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Erreur de connexion"
                )
            }
        }
    }

    fun register() {
        val state = _uiState.value
        if (state.registerUsername.isBlank() || state.registerEmail.isBlank() || state.registerPassword.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Username, email et mot de passe requis")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null, infoMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                authRegisterUseCase(
                    username = state.registerUsername.trim(),
                    email = state.registerEmail.trim(),
                    password = state.registerPassword
                )
            }.onSuccess { session ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    user = session.user.toUi(),
                    registerPassword = "",
                    infoMessage = "Compte cree et connecte"
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Erreur lors de la creation du compte"
                )
            }
        }
    }

    fun fetchMe() {
        val token = _uiState.value.token ?: return

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
            loginPassword = "",
            registerPassword = "",
            infoMessage = "Deconnecte"
        )
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = AppContainer.authRepository
                return AccountViewModel(
                    authLoginUseCase = AuthLoginUseCase(repository),
                    authRegisterUseCase = AuthRegisterUseCase(repository),
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
        createdAt = createdAt
    )
}
