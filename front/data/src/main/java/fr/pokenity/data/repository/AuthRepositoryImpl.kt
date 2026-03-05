package fr.pokenity.data.repository

import fr.pokenity.data.core.AuthSessionState
import fr.pokenity.data.model.AuthCharacter
import fr.pokenity.data.model.AuthenticatedSession
import fr.pokenity.data.model.AuthenticatedUser
import fr.pokenity.data.remote.auth.AuthApiService
import fr.pokenity.data.remote.auth.AuthCharacterDto
import fr.pokenity.data.remote.auth.AuthSessionDto
import fr.pokenity.data.remote.auth.AuthUserDto
import kotlinx.coroutines.flow.StateFlow

class AuthRepositoryImpl internal constructor(
    private val authApiService: AuthApiService
) : AuthRepository {

    override val token: StateFlow<String?> = AuthSessionState.token

    override suspend fun checkEmailExists(email: String): Boolean {
        return authApiService.checkEmailExists(email = email)
    }

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
        val resolvedAvatar = authApiService.normalizeMediaUrl(
            avatarFileName?.ifBlank { null } ?: avatarUrlRaw
        )
        val resolvedImage = authApiService.normalizeMediaUrl(
            imageFileName?.ifBlank { null } ?: imageUrlRaw
        )
        return AuthCharacter(
            id = id,
            name = name,
            avatarUrl = resolvedAvatar,
            imageUrl = resolvedImage
        )
    }
}
