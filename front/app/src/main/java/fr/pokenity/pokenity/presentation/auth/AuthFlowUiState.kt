package fr.pokenity.pokenity.presentation.auth

data class AuthFlowUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Login screen
    val email: String = "",
    val emailChecked: Boolean = false,
    val emailExists: Boolean = false,
    val loginPassword: String = "",

    // Register screen
    val registerUsername: String = "",
    val registerEmail: String = "",
    val registerPassword: String = "",

    // Character selection
    val characters: List<AuthCharacterUiModel> = emptyList(),
    val selectedCharacterIndex: Int = 0
)

data class AuthCharacterUiModel(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val imageUrl: String
)
