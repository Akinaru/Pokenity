package fr.pokenity.pokenity.domain.repository

import fr.pokenity.pokenity.domain.model.AuthenticatedSession
import fr.pokenity.pokenity.domain.model.AuthenticatedUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val token: StateFlow<String?>

    suspend fun login(identifier: String, password: String): AuthenticatedSession
    suspend fun register(username: String, email: String, password: String): AuthenticatedSession
    suspend fun fetchAuthenticatedUser(): AuthenticatedUser
    fun logout()
}
