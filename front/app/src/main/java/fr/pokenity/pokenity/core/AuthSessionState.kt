package fr.pokenity.pokenity.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthSessionState {
    private const val PREFS_NAME = "pokenity_auth"
    private const val KEY_TOKEN = "auth_token"

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private var prefsInitialized = false
    private var tokenStore: android.content.SharedPreferences? = null

    fun initialize(context: Context) {
        if (prefsInitialized) return

        tokenStore = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _token.value = tokenStore?.getString(KEY_TOKEN, null)
        prefsInitialized = true
    }

    fun setToken(value: String?) {
        _token.value = value
        tokenStore?.edit()?.putString(KEY_TOKEN, value)?.apply()
    }

    fun clear() {
        setToken(null)
    }
}
