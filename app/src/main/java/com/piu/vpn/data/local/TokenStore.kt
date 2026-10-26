package com.piu.vpn.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.piu.vpn.data.api.dto.TokenPairResponse
import java.util.UUID

class TokenStore(context: Context) {

    private val prefs: SharedPreferences = try {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "secure_tokens",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    } catch (_: Exception) {
        context.getSharedPreferences("secure_tokens_fallback", Context.MODE_PRIVATE)
    }

    val installId: String
        get() {
            var id = prefs.getString(KEY_INSTALL_ID, null)
            if (id == null) {
                id = UUID.randomUUID().toString()
                prefs.edit { putString(KEY_INSTALL_ID, id) }
            }
            return id
        }

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) {
            prefs.edit {
                if (value == null) remove(KEY_ACCESS_TOKEN) else putString(KEY_ACCESS_TOKEN, value)
            }
        }

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) {
            prefs.edit {
                if (value == null) remove(KEY_REFRESH_TOKEN) else putString(KEY_REFRESH_TOKEN, value)
            }
        }

    var expiresAt: String?
        get() = prefs.getString(KEY_EXPIRES_AT, null)
        set(value) {
            prefs.edit {
                if (value == null) remove(KEY_EXPIRES_AT) else putString(KEY_EXPIRES_AT, value)
            }
        }

    fun savePair(pair: TokenPairResponse) {
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, pair.accessToken)
            putString(KEY_REFRESH_TOKEN, pair.refreshToken)
            putString(KEY_EXPIRES_AT, pair.expiresAt)
        }
    }

    fun save(accessToken: String, refreshToken: String, expiresAt: String) {
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putString(KEY_EXPIRES_AT, expiresAt)
        }
    }

    fun hasToken(): Boolean {
        return !accessToken.isNullOrEmpty()
    }

    fun clear() {
        prefs.edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_EXPIRES_AT)
        }
    }

    companion object {
        private const val KEY_INSTALL_ID = "install_id"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EXPIRES_AT = "expires_at"
    }
}
