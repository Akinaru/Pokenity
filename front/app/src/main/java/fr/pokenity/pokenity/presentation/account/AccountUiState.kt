package fr.pokenity.pokenity.presentation.account

data class AccountUser(
    val id: String,
    val username: String,
    val email: String,
    val createdAt: String? = null
)

enum class AuthMode {
    LOGIN,
    REGISTER
}

data class AccountUiState(
    val isLoading: Boolean = false,
    val mode: AuthMode = AuthMode.LOGIN,
    val token: String? = null,
    val user: AccountUser? = null,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val loginIdentifier: String = "",
    val loginPassword: String = "",
    val registerUsername: String = "",
    val registerEmail: String = "",
    val registerPassword: String = ""
)
