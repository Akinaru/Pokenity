package fr.pokenity.data.remote.auth

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal class AuthApiService(
    private val baseUrl: String = "https://apipoke.cloud.akinaru.fr/api"
) {
    private val baseOrigin: String = run {
        val parsed = URL(baseUrl)
        "${parsed.protocol}://${parsed.host}${if (parsed.port != -1) ":${parsed.port}" else ""}"
    }

    fun register(
        username: String,
        email: String,
        password: String,
        characterId: String? = null
    ): AuthSessionDto {
        val payload = JSONObject()
            .put("username", username)
            .put("email", email)
            .put("password", password)
        if (!characterId.isNullOrBlank()) {
            payload.put("characterId", characterId)
        }
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
            parseAuthUser(root)
        } finally {
            connection.disconnect()
        }
    }

    internal fun characters(): List<AuthCharacterDto> {
        val connection = (URL("$baseUrl/characters").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
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

            val root = JSONObject(body)
            val entries = root.optJSONArray("characters") ?: return emptyList()
            buildList {
                for (index in 0 until entries.length()) {
                    val item = entries.getJSONObject(index)
                    add(
                        AuthCharacterDto(
                            id = item.getString("id"),
                            name = item.getString("name"),
                            avatarUrl = normalizeMediaUrl(
                                item.optString("avatarFileName").ifBlank {
                                    item.optString("avatarUrl")
                                }
                            ),
                            imageUrl = normalizeMediaUrl(
                                item.optString("imageFileName").ifBlank {
                                    item.optString("imageUrl")
                                }
                            )
                        )
                    )
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun normalizeMediaUrl(rawUrl: String?): String {
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
        // Plain filename => handled as local app asset (no host prefix)
        if (!value.contains("/")) return value
        if (value.startsWith("/")) return "$baseOrigin$value"
        return "$baseOrigin/$value"
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
                user = parseAuthUser(user)
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun parseAuthUser(root: JSONObject): AuthUserDto {
        return AuthUserDto(
            id = root.getString("id"),
            username = root.getString("username"),
            email = root.getString("email"),
            createdAt = root.optString("createdAt").ifBlank { null },
            characterId = root.optString("characterId").ifBlank { null },
            character = parseAuthCharacter(root.optJSONObject("character"))
        )
    }

    private fun parseAuthCharacter(raw: JSONObject?): AuthCharacterDto? {
        if (raw == null) return null
        val id = raw.optString("id").trim()
        val name = raw.optString("name").trim()
        if (id.isBlank() || name.isBlank()) return null
        return AuthCharacterDto(
            id = id,
            name = name,
            avatarUrl = normalizeMediaUrl(
                raw.optString("avatarFileName").ifBlank {
                    raw.optString("avatarUrl")
                }
            ),
            imageUrl = normalizeMediaUrl(
                raw.optString("imageFileName").ifBlank {
                    raw.optString("imageUrl")
                }
            )
        )
    }

    private fun parseApiError(raw: String, fallback: String): String {
        return runCatching {
            JSONObject(raw).optString("error").ifBlank { fallback }
        }.getOrDefault(fallback)
    }
}

