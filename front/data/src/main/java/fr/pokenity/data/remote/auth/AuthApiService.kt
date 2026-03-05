package fr.pokenity.data.remote.auth

import com.google.gson.Gson
import retrofit2.Response
import java.net.URL

internal class AuthApiService(
    private val api: AuthRetrofitApi,
    private val baseUrl: String = "https://apipoke.cloud.akinaru.fr/api"
) {
    private val baseOrigin: String = run {
        val parsed = URL(baseUrl)
        "${parsed.protocol}://${parsed.host}${if (parsed.port != -1) ":${parsed.port}" else ""}"
    }

    private val gson = Gson()

    suspend fun register(
        username: String,
        email: String,
        password: String,
        characterId: String? = null
    ): AuthSessionDto {
        val body = RegisterRequestBody(
            username = username,
            email = email,
            password = password,
            characterId = characterId
        )
        val response = api.register(body)
        return handleResponse(response)
    }

    suspend fun login(identifier: String, password: String): AuthSessionDto {
        val body = LoginRequestBody(identifier = identifier, password = password)
        val response = api.login(body)
        return handleResponse(response)
    }

    suspend fun me(token: String): AuthUserDto {
        val response = api.me("Bearer $token")
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: ""
            throw IllegalStateException(parseApiError(errorBody, "Erreur API (${response.code()})"))
        }
        return response.body()?.user
            ?: throw IllegalStateException("Reponse vide du serveur")
    }

    internal suspend fun characters(): List<AuthCharacterDto> {
        val response = api.characters()
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: ""
            throw IllegalStateException(parseApiError(errorBody, "Erreur API (${response.code()})"))
        }
        return response.body()?.characters?.map { dto ->
            AuthCharacterDto(
                id = dto.id,
                name = dto.name,
                avatarFileName = dto.avatarFileName,
                avatarUrlRaw = dto.avatarUrlRaw,
                imageFileName = dto.imageFileName,
                imageUrlRaw = dto.imageUrlRaw
            )
        } ?: emptyList()
    }

    internal fun normalizeMediaUrl(rawUrl: String?): String {
        val value = (rawUrl ?: "").trim()
        if (value.isBlank()) return ""
        val slashNormalized = value.replace("\\", "/")
        if (
            slashNormalized.contains("/uploads/characters/") ||
            slashNormalized.startsWith("uploads/characters/")
        ) {
            return slashNormalized.substringAfterLast("/")
        }
        if (value.startsWith("http://") || value.startsWith("https://")) return value
        if (!value.contains("/")) return value
        if (value.startsWith("/")) return "$baseOrigin$value"
        return "$baseOrigin/$value"
    }

    private fun <T> handleResponse(response: Response<T>): T {
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: ""
            throw IllegalStateException(parseApiError(errorBody, "Erreur API (${response.code()})"))
        }
        return response.body() ?: throw IllegalStateException("Reponse vide du serveur")
    }

    private fun parseApiError(raw: String, fallback: String): String {
        return runCatching {
            gson.fromJson(raw, AuthErrorDto::class.java)?.error?.ifBlank { fallback } ?: fallback
        }.getOrDefault(fallback)
    }
}
