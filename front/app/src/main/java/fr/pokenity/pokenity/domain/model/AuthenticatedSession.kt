package fr.pokenity.pokenity.domain.model

data class AuthenticatedSession(
    val token: String,
    val user: AuthenticatedUser
)
