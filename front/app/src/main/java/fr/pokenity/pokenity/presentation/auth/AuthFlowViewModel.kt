package fr.pokenity.pokenity.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.data.core.AppContainer
import fr.pokenity.data.core.AuthSessionState
import fr.pokenity.data.model.AuthCharacter
import fr.pokenity.pokenity.domain.usecase.AuthCheckEmailExistsUseCase
import fr.pokenity.pokenity.domain.usecase.AuthFetchCharactersUseCase
import fr.pokenity.pokenity.domain.usecase.AuthLoginUseCase
import fr.pokenity.pokenity.domain.usecase.AuthRegisterUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthFlowViewModel(
    private val authCheckEmailExistsUseCase: AuthCheckEmailExistsUseCase,
    private val authLoginUseCase: AuthLoginUseCase,
    private val authRegisterUseCase: AuthRegisterUseCase,
    private val authFetchCharactersUseCase: AuthFetchCharactersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthFlowUiState())
    val uiState: StateFlow<AuthFlowUiState> = _uiState.asStateFlow()

    // --- Welcome ---

    fun onWelcomeDone() {
        AuthSessionState.markFirstLaunchDone()
    }

    // --- Login screen ---

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(
            email = value,
            emailChecked = false,
            emailExists = false,
            loginPassword = "",
            errorMessage = null
        )
    }

    fun updateLoginPassword(value: String) {
        _uiState.value = _uiState.value.copy(loginPassword = value, errorMessage = null)
    }

    fun checkEmail(onEmailNotFound: () -> Unit) {
        val state = _uiState.value
        val email = state.email.trim()

        if (email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Veuillez entrer votre email.")
            return
        }
        if (!email.contains("@")) {
            _uiState.value = state.copy(errorMessage = "Format d'email invalide.")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                authCheckEmailExistsUseCase(email = email)
            }.onSuccess { exists ->
                if (exists) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        emailChecked = true,
                        emailExists = true,
                        loginPassword = "",
                        errorMessage = null
                    )
                    return@onSuccess
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    emailChecked = false,
                    emailExists = false,
                    registerEmail = email,
                    registerUsername = "",
                    registerPassword = "",
                    registerEmailConfirmed = true,
                    errorMessage = null
                )
                launch(Dispatchers.Main) { onEmailNotFound() }
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Erreur lors de la verification de l'email"
                )
            }
        }
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.loginPassword.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Email et mot de passe requis.")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                authLoginUseCase(
                    identifier = state.email.trim(),
                    password = state.loginPassword
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loginPassword = ""
                )
                launch(Dispatchers.Main) { onSuccess() }
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Erreur de connexion"
                )
            }
        }
    }

    // --- Register screen ---

    fun updateRegisterUsername(value: String) {
        _uiState.value = _uiState.value.copy(registerUsername = value, errorMessage = null)
    }

    fun updateRegisterEmail(value: String) {
        _uiState.value = _uiState.value.copy(
            registerEmail = value,
            registerUsername = "",
            registerPassword = "",
            registerEmailConfirmed = false,
            errorMessage = null
        )
    }

    fun updateRegisterPassword(value: String) {
        _uiState.value = _uiState.value.copy(registerPassword = value, errorMessage = null)
    }

    fun confirmRegisterEmail(onExistingAccount: () -> Unit) {
        val state = _uiState.value
        val email = state.registerEmail.trim()

        if (email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Veuillez entrer votre email.")
            return
        }
        if (!email.contains("@")) {
            _uiState.value = state.copy(errorMessage = "Format d'email invalide.")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                authCheckEmailExistsUseCase(email = email)
            }.onSuccess { exists ->
                if (exists) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        email = email,
                        emailChecked = true,
                        emailExists = true,
                        loginPassword = "",
                        registerEmailConfirmed = false,
                        registerUsername = "",
                        registerPassword = "",
                        errorMessage = null
                    )
                    launch(Dispatchers.Main) { onExistingAccount() }
                    return@onSuccess
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    registerEmail = email,
                    registerEmailConfirmed = true,
                    errorMessage = null
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Erreur lors de la verification de l'email"
                )
            }
        }
    }

    fun goToCharacterSelection(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!state.registerEmailConfirmed) {
            _uiState.value = state.copy(errorMessage = "Valide d'abord ton email.")
            return
        }
        if (state.registerUsername.isBlank() || state.registerEmail.isBlank() || state.registerPassword.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Username, email et mot de passe requis.")
            return
        }
        if (!state.registerEmail.trim().contains("@")) {
            _uiState.value = state.copy(errorMessage = "Format d'email invalide.")
            return
        }
        if (state.registerPassword.length < 6) {
            _uiState.value = state.copy(errorMessage = "Le mot de passe doit contenir au moins 6 caracteres.")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                authFetchCharactersUseCase()
            }.onSuccess {
                if (it.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Aucun dresseur disponible."
                    )
                    return@onSuccess
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    characters = it.map { character -> character.toUi() },
                    selectedCharacterIndex = 0
                )
                launch(Dispatchers.Main) { onSuccess() }
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Erreur lors du chargement des dresseurs"
                )
            }
        }
    }

    fun selectPreviousCharacter() {
        val state = _uiState.value
        if (state.characters.isEmpty()) return
        val nextIndex = if (state.selectedCharacterIndex <= 0) {
            state.characters.lastIndex
        } else {
            state.selectedCharacterIndex - 1
        }
        _uiState.value = state.copy(selectedCharacterIndex = nextIndex)
    }

    fun selectNextCharacter() {
        val state = _uiState.value
        if (state.characters.isEmpty()) return
        val nextIndex = if (state.selectedCharacterIndex >= state.characters.lastIndex) {
            0
        } else {
            state.selectedCharacterIndex + 1
        }
        _uiState.value = state.copy(selectedCharacterIndex = nextIndex)
    }

    fun registerWithSelectedCharacter(
        onSuccess: () -> Unit,
        onFormError: () -> Unit
    ) {
        val state = _uiState.value
        val selectedCharacter = state.characters.getOrNull(state.selectedCharacterIndex)
        if (selectedCharacter == null) {
            _uiState.value = state.copy(errorMessage = "Choisis un dresseur.")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                authRegisterUseCase(
                    username = state.registerUsername.trim(),
                    email = state.registerEmail.trim(),
                    password = state.registerPassword,
                    characterId = selectedCharacter.id
                )
            }.onSuccess {
                AuthSessionState.setNewAccount(false)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    registerPassword = ""
                )
                launch(Dispatchers.Main) { onSuccess() }
            }.onFailure {
                val message = it.message ?: "Erreur lors de la creation du compte"
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = message
                )
                if (isFormErrorMessage(message)) {
                    launch(Dispatchers.Main) { onFormError() }
                }
            }
        }
    }

    // --- Profile setup ---

    fun onProfileSetupDone() {
        AuthSessionState.setNewAccount(false)
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = AppContainer.authRepository
                return AuthFlowViewModel(
                    authCheckEmailExistsUseCase = AuthCheckEmailExistsUseCase(repository),
                    authLoginUseCase = AuthLoginUseCase(repository),
                    authRegisterUseCase = AuthRegisterUseCase(repository),
                    authFetchCharactersUseCase = AuthFetchCharactersUseCase(repository)
                ) as T
            }
        }
    }
}

private fun isFormErrorMessage(message: String): Boolean {
    val normalized = message.lowercase()
    return normalized.contains("email") ||
        normalized.contains("username") ||
        normalized.contains("password") ||
        normalized.contains("mot de passe")
}

private fun AuthCharacter.toUi(): AuthCharacterUiModel {
    return AuthCharacterUiModel(
        id = id,
        name = name,
        avatarUrl = avatarUrl,
        imageUrl = imageUrl
    )
}
