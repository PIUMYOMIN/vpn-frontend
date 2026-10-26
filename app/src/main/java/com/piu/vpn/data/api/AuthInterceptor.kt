package com.piu.vpn.data.api

import com.piu.vpn.data.api.dto.TokenPairResponse
import com.piu.vpn.data.local.TokenStore
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

class AuthInterceptor(
    private val tokenStore: TokenStore,
    private val baseUrl: String,
) : Interceptor {

    private val json = Json { ignoreUnknownKeys = true }
    private val refreshClient = OkHttpClient()

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath

        // Auth endpoints: add X-Install-ID only (no Authorization)
        if (path.startsWith("/auth/")) {
            val req = original.newBuilder()
                .header("X-Install-ID", tokenStore.installId)
                .build()
            return chain.proceed(req)
        }

        // Other endpoints: add both headers
        val authed = original.newBuilder()
            .header("X-Install-ID", tokenStore.installId)
            .apply {
                tokenStore.accessToken?.let { header("Authorization", "Bearer $it") }
            }
            .build()

        var response = chain.proceed(authed)

        // 401 → try refresh (once)
        if (response.code == 401 && tokenStore.refreshToken != null) {
            val newAccess = refreshTokens()
            if (newAccess != null) {
                response.close()
                val retry = original.newBuilder()
                    .header("Authorization", "Bearer $newAccess")
                    .header("X-Install-ID", tokenStore.installId)
                    .build()
                response = chain.proceed(retry)
            }
        }

        return response
    }

    // ============================================================
    // Refresh (synchronized to prevent parallel refresh calls)
    // ============================================================

    @Synchronized
    private fun refreshTokens(): String? {
        val refresh = tokenStore.refreshToken ?: return null

        val body = """{"refresh_token":"$refresh"}"""
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$baseUrl/auth/refresh")
            .post(body)
            .header("X-Install-ID", tokenStore.installId)
            .build()

        return try {
            refreshClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    tokenStore.clear()
                    return null
                }
                val bodyStr = resp.body?.string() ?: return null
                val obj = json.parseToJsonElement(bodyStr).jsonObject
                val newAccess = obj["access_token"]?.jsonPrimitive?.content ?: return null
                val newRefresh = obj["refresh_token"]?.jsonPrimitive?.content ?: return null
                val newExpires = obj["expires_at"]?.jsonPrimitive?.content ?: ""

                tokenStore.savePair(TokenPairResponse(newAccess, newRefresh, newExpires))
                newAccess
            }
        } catch (e: Exception) {
            tokenStore.clear()
            null
        }
    }
}