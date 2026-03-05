package fr.pokenity.pokenity.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.pokenity.data.core.AppContainer
import fr.pokenity.data.core.AuthSessionState
import fr.pokenity.data.model.AuthCharacter
import fr.pokenity.pokenity.domain.usecase.AuthFetchCharactersUseCase
import fr.pokenity.pokenity.domain.usecase.AuthLoginUseCase
import fr.pokenity.pokenity.domain.usecase.AuthRegisterUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthFlowViewModel(
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
            errorMessage = null
        )
    }

    fun updateLoginPassword(value: String) {
        _uiState.value = _uiState.value.copy(loginPassword = value, errorMessage = null)
    }

    /**
     * Fake check email: simule une verification puis retourne toujours true (compte existe).
     * TODO: remplacer par un vrai appel API quand l'endpoint sera pret.
     */
    fun checkEmail() {
        val state = _uiState.value
        val email = state.email.trim()

        if (email.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Veuillez entrer votre email.")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            delay(400) // fake delai reseau
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                emailChecked = true,
                emailExists = true // TODO: remplacer par le vrai resultat
            )
        }
    }

    fun prepareRegisterWithEmail() {
        val email = _uiState.value.email.trim()
        _uiState.value = _uiState.value.copy(
            registerEmail = email,
            registerUsername = "",
            registerPassword = "",
            errorMessage = null
        )
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
        _uiState.value = _uiState.value.copy(registerEmail = value, errorMessage = null)
    }

    fun updateRegisterPassword(value: String) {
        _uiState.value = _uiState.value.copy(registerPassword = value, errorMessage = null)
    }

    fun goToCharacterSelection(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.registerUsername.isBlank() || state.registerEmail.isBlank() || state.registerPassword.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Username, email et mot de passe requis.")
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

    fun registerWithSelectedCharacter(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.registerUsername.isBlank() || state.registerEmail.isBlank() || state.registerPassword.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Username, email et mot de passe requis.")
            return
        }

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
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = it.message ?: "Erreur lors de la creation du compte"
                )
            }
        }
    }

    // --- Profile setup ---

    fun onProfileSetupDone() {
        AuthSessionState.setNewAccount(false)
    }

    // --- Reset ---

    fun resetLoginState() {
        _uiState.value = _uiState.value.copy(
            emailChecked = false,
            emailExists = false,
            loginPassword = "",
            errorMessage = null
        )
    }

    companion object {
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = AppContainer.authRepository
                return AuthFlowViewModel(
                    authLoginUseCase = AuthLoginUseCase(repository),
                    authRegisterUseCase = AuthRegisterUseCase(repository),
                    authFetchCharactersUseCase = AuthFetchCharactersUseCase(repository)
                ) as T
            }
        }
    }
}

private fun AuthCharacter.toUi(): AuthCharacterUiModel {
    return AuthCharacterUiModel(
        id = id,
        name = name,
        avatarUrl = avatarUrl,
        imageUrl = imageUrl
    )
}
