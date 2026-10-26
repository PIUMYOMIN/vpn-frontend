package com.piu.vpn.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferences private constructor(private val context: Context) {

    companion object {
        private val LANGUAGE = stringPreferencesKey("language")
        private val THEME = stringPreferencesKey("theme")

        @Volatile
        private var instance: UserPreferences? = null

        fun get(context: Context): UserPreferences =
            instance ?: synchronized(this) {
                instance ?: UserPreferences(context.applicationContext).also { instance = it }
            }
    }

    val language: Flow<String> = context.dataStore.data.map { it[LANGUAGE] ?: "my" }
    val theme: Flow<String> = context.dataStore.data.map { it[THEME] ?: "system" }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[LANGUAGE] = lang }
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[THEME] = theme }
    }
}