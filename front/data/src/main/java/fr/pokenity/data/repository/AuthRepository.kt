package fr.pokenity.data.repository

import fr.pokenity.data.model.AuthCharacter
import fr.pokenity.data.model.AuthenticatedSession
import fr.pokenity.data.model.AuthenticatedUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val token: StateFlow<String?>

    suspend fun login(identifier: String, password: String): AuthenticatedSession
    suspend fun register(
        username: String,
        email: String,
        password: String,
        characterId: String? = null
    ): AuthenticatedSession
    suspend fun fetchCharacters(): List<AuthCharacter>
    suspend fun fetchAuthenticatedUser(): AuthenticatedUser
    fun logout()
}
