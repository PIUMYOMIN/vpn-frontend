package com.piu.vpn.data.repository

import com.piu.vpn.data.api.ApiClient
import com.piu.vpn.data.api.dto.EntitlementResponse
import com.piu.vpn.data.api.dto.VpnConfigResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.Response
import java.io.IOException

class VpnRepository {

    private val api = ApiClient.api
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getEntitlement(): ApiResult<EntitlementResponse> = call {
        api.entitlement()
    }

    // Ask the server whether this owner selected the rare fallback grant.
    suspend fun requestFallbackEntitlement(): ApiResult<EntitlementResponse> = call {
        api.fallbackEntitlement()
    }

    // 403 no_entitlement ကို Error မဟုတ်ဘဲ "no entitlement" အဖြစ် ပြန်ပေး
    suspend fun getVpnConfig(): ApiResult<VpnConfigResponse> {
        return try {
            val resp = api.vpnConfig()
            val body = resp.body()
            if (resp.isSuccessful && body != null) {
                ApiResult.Success(body)
            } else if (resp.code() == 403) {
                ApiResult.Error("no_entitlement", 403)
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