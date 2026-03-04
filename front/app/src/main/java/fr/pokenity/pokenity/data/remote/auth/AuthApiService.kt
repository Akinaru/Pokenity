package fr.pokenity.pokenity.data.remote.auth

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AuthApiService(
    private val baseUrl: String = "https://apipoke.cloud.akinaru.fr/api"
) {

    fun register(username: String, email: String, password: String): AuthSessionDto {
        val payload = JSONObject()
            .put("username", username)
            .put("email", email)
            .put("password", password)
        return postAuth("$baseUrl/auth/register", payload.toString())
    }

    fun login(identifier: String, password: String): AuthSessionDto {
        val payload = JSONObject()
            .put("identifier", identifier)
            .put("password", password)
        return postAuth("$baseUrl/auth/login", payload.toString())
    }

    fun me(token: String): AuthUserDto {
        val connection = (URL("$baseUrl/auth/me").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Authorization", "Bearer $token")
        }

        return try {
            val status = connection.responseCode
            val body = if (status in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            if (status !in 200..299) {
                throw IllegalStateException(parseApiError(body, "Erreur API ($status)"))
            }

            val root = JSONObject(body).getJSONObject("user")
            AuthUserDto(
                id = root.getString("id"),
                username = root.getString("username"),
                email = root.getString("email"),
                createdAt = root.optString("createdAt")
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun postAuth(url: String, body: String): AuthSessionDto {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }

        return try {
            connection.outputStream.use { output ->
                output.write(body.toByteArray())
            }

            val status = connection.responseCode
            val response = if (status in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            if (status !in 200..299) {
                throw IllegalStateException(parseApiError(response, "Erreur API ($status)"))
            }

            val root = JSONObject(response)
            val user = root.getJSONObject("user")
            AuthSessionDto(
                token = root.getString("token"),
                user = AuthUserDto(
                    id = user.getString("id"),
                    username = user.getString("username"),
                    email = user.getString("email"),
                    createdAt = user.optString("createdAt")
                )
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun parseApiError(raw: String, fallback: String): String {
        return runCatching {
            JSONObject(raw).optString("error").ifBlank { fallback }
        }.getOrDefault(fallback)
    }
}

data class AuthSessionDto(
    val token: String,
    val user: AuthUserDto
)

data class AuthUserDto(
    val id: String,
    val username: String,
    val email: String,
    val createdAt: String? = null
)
