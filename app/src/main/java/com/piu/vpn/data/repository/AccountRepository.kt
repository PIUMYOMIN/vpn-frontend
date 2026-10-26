package com.piu.vpn.data.repository

import com.piu.vpn.data.api.ApiClient
import com.piu.vpn.data.api.dto.DeleteAccountRequest
import com.piu.vpn.data.api.dto.DeviceListResponse
import com.piu.vpn.data.api.dto.LogoutRequest
import com.piu.vpn.data.api.dto.UserResponse
import com.piu.vpn.data.local.TokenStore
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.Response
import java.io.IOException

class AccountRepository(
    private val tokenStore: TokenStore,
) {
    private val api = ApiClient.api
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getMe(): ApiResult<UserResponse> = call { api.me() }

    suspend fun getDevices(): ApiResult<DeviceListResponse> = call { api.devices() }

    suspend fun deleteDevice(id: String): ApiResult<Unit> = callUnit {
        api.deleteDevice(id)
    }

    suspend fun logout(): ApiResult<Unit> {
        val refresh = tokenStore.refreshToken ?: return ApiResult.Success(Unit)
        return try {
            api.logout(LogoutRequest(refresh))
            tokenStore.clear()
            ApiResult.Success(Unit)
        } catch (e: IOException) {
            tokenStore.clear()
            ApiResult.NetworkError
        } catch (e: Exception) {
            tokenStore.clear()
            ApiResult.Error(e.message ?: "Logout failed")
        }
    }

    suspend fun deleteAccount(password: String): ApiResult<Unit> {
        return try {
            val resp = api.deleteAccount(DeleteAccountRequest(password))
            if (resp.isSuccessful) {
                tokenStore.clear()
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(parseError(resp), resp.code())
            }
        } catch (e: IOException) {
            ApiResult.NetworkError
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    private suspend fun <T> call(block: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val resp = block()
            val body = resp.body()
            if (resp.isSuccessful && body != null) ApiResult.Success(body)
            else ApiResult.Error(parseError(resp), resp.code())
        } catch (e: IOException) {
            ApiResult.NetworkError
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    private suspend fun callUnit(block: suspend () -> Response<*>): ApiResult<Unit> {
        return try {
            val resp = block()
            if (resp.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error(parseError(resp), resp.code())
        } catch (e: IOException) {
            ApiResult.NetworkError
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun parseError(resp: Response<*>): String {
        val raw = resp.errorBody()?.string() ?: return "HTTP ${resp.code()}"
        return try {
            json.parseToJsonElement(raw).jsonObject["error"]
                ?.jsonPrimitive?.content ?: raw
        } catch (e: Exception) {
            raw
        }
    }
}