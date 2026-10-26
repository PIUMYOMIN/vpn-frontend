package com.piu.vpn.data.api

import com.piu.vpn.BuildConfig
import com.piu.vpn.data.local.TokenStore
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    lateinit var tokenStore: TokenStore
        private set

    private var _api: VpnApi? = null

    val api: VpnApi
        get() = _api ?: error("ApiClient not initialized. Call ApiClient.init(context) first.")

    val baseUrl: String get() = BuildConfig.API_BASE_URL

    fun init(store: TokenStore) {
        if (_api != null) return
        this.tokenStore = store

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
        }

        // Logging (DEBUG only)
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val okHttp = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenStore, BuildConfig.API_BASE_URL))
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttp)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        _api = retrofit.create(VpnApi::class.java)
    }
}