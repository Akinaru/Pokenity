package fr.pokenity.data.remote.auth

import com.google.gson.annotations.SerializedName

internal data class AuthSessionDto(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: AuthUserDto
)

internal data class AuthUserDto(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("characterId") val characterId: String? = null,
    @SerializedName("character") val character: AuthCharacterDto? = null
)

internal data class AuthCharacterDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("avatarFileName") val avatarFileName: String? = null,
    @SerializedName("avatarUrl") val avatarUrlRaw: String? = null,
    @SerializedName("imageFileName") val imageFileName: String? = null,
    @SerializedName("imageUrl") val imageUrlRaw: String? = null
)

internal data class AuthMeResponseDto(
    @SerializedName("user") val user: AuthUserDto
)

internal data class AuthEmailExistsResponseDto(
    @SerializedName("exists") val exists: Boolean
)

internal data class AuthUsersResponseDto(
    @SerializedName("users") val users: List<AuthUserDto>?
)

internal data class AuthCharactersResponseDto(
    @SerializedName("characters") val characters: List<AuthCharacterDto>?
)

internal data class AuthErrorDto(
    @SerializedName("error") val error: String?
)

// Request bodies
internal data class LoginRequestBody(
    @SerializedName("identifier") val identifier: String,
    @SerializedName("password") val password: String
)

internal data class RegisterRequestBody(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("characterId") val characterId: String? = null
)
