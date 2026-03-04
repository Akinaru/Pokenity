package fr.pokenity.pokenity.domain.model

data class AuthenticatedUser(
    val id: String,
    val username: String,
    val email: String,
    val createdAt: String? = null
)
