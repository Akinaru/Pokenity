package fr.pokenity.data.model

data class AuthenticatedUser(
    val id: String,
    val username: String,
    val email: String,
    val createdAt: String? = null,
    val character: AuthCharacter? = null
)
