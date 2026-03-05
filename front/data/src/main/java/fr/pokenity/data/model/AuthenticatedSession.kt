package fr.pokenity.data.model

data class AuthenticatedSession(
    val token: String,
    val user: AuthenticatedUser
)
