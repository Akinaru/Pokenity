package fr.pokenity.pokenity.data.repository

import fr.pokenity.pokenity.core.AuthSessionState
import fr.pokenity.pokenity.data.remote.auth.AuthCharacterDto
import fr.pokenity.pokenity.data.remote.auth.AuthApiService
import fr.pokenity.pokenity.data.remote.auth.AuthSessionDto
import fr.pokenity.pokenity.data.remote.auth.AuthUserDto
import fr.pokenity.pokenity.domain.model.AuthCharacter
import fr.pokenity.pokenity.domain.model.AuthenticatedSession
import fr.pokenity.pokenity.domain.model.AuthenticatedUser
import fr.pokenity.pokenity.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class AuthRepositoryImpl(
    private val authApiService: AuthApiService
) : AuthRepository {

    override val token: StateFlow<String?> = AuthSessionState.token

    override suspend fun login(identifier: String, password: String): AuthenticatedSession {
        val session = authApiService.login(identifier = identifier, password = password)
        AuthSessionState.setToken(session.token)
        return session.toDomain()
    }

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        characterId: String?
    ): AuthenticatedSession {
        val session = authApiService.register(
            username = username,
            email = email,
            password = password,
            characterId = characterId
        )
        AuthSessionState.setToken(session.token)
        return session.toDomain()
    }

    override suspend fun fetchCharacters(): List<AuthCharacter> {
        return authApiService.characters().map { it.toDomain() }
    }

    override suspend fun fetchAuthenticatedUser(): AuthenticatedUser {
        val currentToken = token.value ?: throw IllegalStateException("Aucune session active.")
        return authApiService.me(token = currentToken).toDomain()
    }

    override fun logout() {
        AuthSessionState.clear()
    }

    private fun AuthSessionDto.toDomain(): AuthenticatedSession {
        return AuthenticatedSession(
            token = token,
            user = user.toDomain()
        )
    }

    private fun AuthUserDto.toDomain(): AuthenticatedUser {
        return AuthenticatedUser(
            id = id,
            username = username,
            email = email,
            createdAt = createdAt,
            character = character?.toDomain()
        )
    }

    private fun AuthCharacterDto.toDomain(): AuthCharacter {
        return AuthCharacter(
            id = id,
            name = name,
            avatarUrl = avatarUrl,
            imageUrl = imageUrl
        )
    }
}
