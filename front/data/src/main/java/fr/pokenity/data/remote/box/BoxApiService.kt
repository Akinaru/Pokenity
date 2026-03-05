package fr.pokenity.data.remote.box

import com.google.gson.Gson

internal class BoxApiService(
    private val api: BoxRetrofitApi
) {
    private val gson = Gson()

    suspend fun getBoxes(): List<BoxDto> {
        val response = api.getBoxes()
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: ""
            throw IllegalStateException(parseApiError(errorBody, "Erreur API (${response.code()})"))
        }
        return response.body()?.boxes ?: emptyList()
    }

    suspend fun getBoxById(boxId: String): BoxDto {
        val response = api.getBoxById(boxId)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: ""
            throw IllegalStateException(parseApiError(errorBody, "Erreur API (${response.code()})"))
        }
        return response.body()?.box ?: throw IllegalStateException("Reponse box vide")
    }

    suspend fun openBox(token: String, boxId: String): OpenBoxResponseDto {
        val response = api.openBox(authorization = "Bearer $token", boxId = boxId)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string() ?: ""
            throw IllegalStateException(parseApiError(errorBody, "Erreur API (${response.code()})"))
        }
        return response.body() ?: throw IllegalStateException("Reponse ouverture box vide")
    }

    private fun parseApiError(raw: String, fallback: String): String {
        return runCatching {
            gson.fromJson(raw, BoxErrorDto::class.java)?.error?.ifBlank { fallback } ?: fallback
        }.getOrDefault(fallback)
    }
}
