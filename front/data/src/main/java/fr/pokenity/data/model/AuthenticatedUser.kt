package fr.pokenity.data.model

data class AuthenticatedUser(
    val id: String,
    val username: String,
    val email: String,
    val xp: Int = 0,
    val createdAt: String? = null,
    val character: AuthCharacter? = null
)
