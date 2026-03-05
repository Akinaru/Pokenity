package fr.pokenity.pokenity.presentation.account

data class AccountUser(
    val id: String,
    val username: String,
    val email: String,
    val xp: Int = 0,
    val createdAt: String? = null,
    val characterName: String? = null,
    val characterAvatarUrl: String? = null,
    val characterImageUrl: String? = null
)

data class AccountUiState(
    val isLoading: Boolean = false,
    val token: String? = null,
    val user: AccountUser? = null,
    val pokemonCollection: Map<Int, Int> = emptyMap(),
    val errorMessage: String? = null,
    val infoMessage: String? = null
)
