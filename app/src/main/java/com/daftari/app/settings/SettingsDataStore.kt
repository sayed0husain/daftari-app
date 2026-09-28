package com.daftari.app.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "daftari_settings")

/**
 * theme_mode values: -1 = follow system, 0 = light, 1 = dark
 */
class SettingsDataStore(private val context: Context) {

    private object Keys {
        val THEME_MODE = intPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language") // "ar" or "en"
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val APP_LOCK_PIN = stringPreferencesKey("app_lock_pin")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val themeMode: Flow<Int> = context.dataStore.data.map { it[Keys.THEME_MODE] ?: -1 }
    val language: Flow<String> = context.dataStore.data.map { it[Keys.LANGUAGE] ?: "ar" }
    val appLockEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.APP_LOCK_ENABLED] ?: false }
    val appLockPin: Flow<String> = context.dataStore.data.map { it[Keys.APP_LOCK_PIN] ?: "" }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.NOTIFICATIONS_ENABLED] ?: false }

    suspend fun setThemeMode(mode: Int) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = lang }
    }

    suspend fun setAppLock(enabled: Boolean, pin: String) {
        context.dataStore.edit {
            it[Keys.APP_LOCK_ENABLED] = enabled
            it[Keys.APP_LOCK_PIN] = pin
        }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }
}
