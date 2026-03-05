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

    suspend fun checkEmailExists(email: String): Boolean {
        val normalizedEmail = email.trim().lowercase()
        val response = api.emailExists(email = normalizedEmail)
        if (response.isSuccessful) {
            return response.body()?.exists ?: false
        }

        // Compat fallback: older API versions may not expose /auth/email-exists yet.
        val errorBody = response.errorBody()?.string() ?: ""
        val isMissingRoute = response.code() == 404 &&
            parseApiError(errorBody, "").lowercase().contains("route not found")
        if (!isMissingRoute) {
            throw IllegalStateException(parseApiError(errorBody, "Erreur API (${response.code()})"))
        }

        val usersResponse = api.users()
        if (!usersResponse.isSuccessful) {
            val usersErrorBody = usersResponse.errorBody()?.string() ?: ""
            throw IllegalStateException(
                parseApiError(usersErrorBody, "Erreur API (${usersResponse.code()})")
            )
        }

        return usersResponse.body()
            ?.users
            ?.any { it.email.equals(normalizedEmail, ignoreCase = true) }
            ?: false
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

    suspend fun inventory(token: String): List<AuthInventoryItemDto> {
        val response = api.inventory("Bearer $token")
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: ""
            throw IllegalStateException(parseApiError(errorBody, "Erreur API (${response.code()})"))
        }
        return response.body()?.inventory ?: emptyList()
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

    // --- Trades ---

    suspend fun getOpenTrades(token: String, limit: Int? = null): List<TradeDto> {
        val response = api.getOpenTrades("Bearer $token", limit)
        return handleResponse(response).trades
    }

    suspend fun getMyTrades(token: String, limit: Int? = null): List<TradeDto> {
        val response = api.getMyTrades("Bearer $token", limit)
        return handleResponse(response).trades
    }

    suspend fun getTradeDetail(token: String, tradeId: String): TradeDto {
        val response = api.getTradeDetail("Bearer $token", tradeId)
        return handleResponse(response).trade
    }

    suspend fun createTrade(token: String, body: CreateTradeRequestBody): TradeDto {
        val response = api.createTrade("Bearer $token", body)
        return handleResponse(response).trade
    }

    suspend fun acceptTrade(token: String, tradeId: String, body: AcceptTradeRequestBody): TradeDto {
        val response = api.acceptTrade("Bearer $token", tradeId, body)
        return handleResponse(response).trade
    }

    suspend fun confirmTrade(token: String, tradeId: String): TradeDto {
        val response = api.confirmTrade("Bearer $token", tradeId)
        return handleResponse(response).trade
    }

    suspend fun cancelTrade(token: String, tradeId: String): TradeDto {
        val response = api.cancelTrade("Bearer $token", tradeId)
        return handleResponse(response).trade
    }

    suspend fun declineTrade(token: String, tradeId: String): TradeDto {
        val response = api.declineTrade("Bearer $token", tradeId)
        return handleResponse(response).trade
    }

    // --- Users ---

    suspend fun getUsers(): List<AuthUserDto> {
        val response = api.users()
        return handleResponse(response).users ?: emptyList()
    }

    suspend fun getUserById(id: String): AuthUserDto {
        val response = api.getUserById(id)
        return handleResponse(response).user
    }

    // --- Inventory ---

    suspend fun getMyInventory(token: String): InventoryResponseDto {
        val response = api.getMyInventory("Bearer $token")
        return handleResponse(response)
    }

    suspend fun getUserInventory(userId: String): InventoryResponseDto {
        val response = api.getUserInventory(userId)
        return handleResponse(response)
    }
}
