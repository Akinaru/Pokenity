package fr.pokenity.data.remote.auth

internal data class AuthSessionDto(
    val token: String,
    val user: AuthUserDto
)

internal data class AuthUserDto(
    val id: String,
    val username: String,
    val email: String,
    val createdAt: String? = null,
    val characterId: String? = null,
    val character: AuthCharacterDto? = null
)

internal data class AuthCharacterDto(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val imageUrl: String
)
