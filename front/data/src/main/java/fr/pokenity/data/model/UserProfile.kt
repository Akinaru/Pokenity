package fr.pokenity.data.model

data class UserProfile(
    val id: String,
    val username: String,
    val xp: Int,
    val character: AuthCharacter? = null
)
