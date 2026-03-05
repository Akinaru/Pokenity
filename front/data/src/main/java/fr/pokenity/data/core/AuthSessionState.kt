package fr.pokenity.data.core

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuthSessionState {
    private const val PREFS_NAME = "pokenity_auth"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_FIRST_LAUNCH = "is_first_launch"
    private const val KEY_NEW_ACCOUNT = "is_new_account"

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _isFirstLaunch = MutableStateFlow(true)
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch.asStateFlow()

    private val _isNewAccount = MutableStateFlow(false)
    val isNewAccount: StateFlow<Boolean> = _isNewAccount.asStateFlow()

    private var prefsInitialized = false
    private var tokenStore: SharedPreferences? = null

    fun initialize(context: Context) {
        if (prefsInitialized) return

        tokenStore = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _token.value = tokenStore?.getString(KEY_TOKEN, null)
        _isFirstLaunch.value = tokenStore?.getBoolean(KEY_FIRST_LAUNCH, true) ?: true
        _isNewAccount.value = tokenStore?.getBoolean(KEY_NEW_ACCOUNT, false) ?: false
        prefsInitialized = true
    }

    fun setToken(value: String?) {
        _token.value = value
        tokenStore?.edit()?.putString(KEY_TOKEN, value)?.apply()
    }

    fun markFirstLaunchDone() {
        _isFirstLaunch.value = false
        tokenStore?.edit()?.putBoolean(KEY_FIRST_LAUNCH, false)?.apply()
    }

    fun setNewAccount(value: Boolean) {
        _isNewAccount.value = value
        tokenStore?.edit()?.putBoolean(KEY_NEW_ACCOUNT, value)?.apply()
    }

    fun clear() {
        setToken(null)
        setNewAccount(false)
    }
}
