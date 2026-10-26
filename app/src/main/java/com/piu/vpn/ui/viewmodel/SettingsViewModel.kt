package com.piu.vpn.ui.viewmodel

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.piu.vpn.data.prefs.UserPreferences
import com.piu.vpn.ui.state.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = UserPreferences.get(application)
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(prefs.language, prefs.theme) { lang, theme ->
                lang to theme
            }.collect { (lang, theme) ->
                _uiState.update { it.copy(language = lang, theme = theme) }
            }
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            prefs.setLanguage(lang)
            // Runtime language change
            val locales = LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            prefs.setTheme(theme)
            // Theme က MainActivity က collectAsState နဲ့ auto-update
        }
    }
}