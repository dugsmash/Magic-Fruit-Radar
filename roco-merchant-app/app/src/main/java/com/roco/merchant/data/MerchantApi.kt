package com.roco.merchant.data

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/** 洛克魔法书/WeGame 后端客户端（X-API-Key 鉴权）
 *  主数据源：GET /api/v1/games/rocom/merchant/info
 *  回退：POST /api/v1/games/rocom/ingame/merchant/info */
class MerchantApi(private val baseUrl: String, private val apiKey: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    /** 拉取商人货架：先 GET /merchant/info，失败则回退 POST /ingame/merchant/info */
    suspend fun fetchMerchant(): MerchantResponse {
        return try {
            getMerchantInfo()
        } catch (first: Exception) {
            try {
                postIngameMerchantInfo()
            } catch (second: Exception) {
                throw Exception(first.message ?: "请求失败")
            }
        }
    }

    private suspend fun getMerchantInfo(): MerchantResponse = kotlinx.coroutines.withContext(
        kotlinx.coroutines.Dispatchers.IO
    ) {
        val url = baseUrl.trimEnd('/') + "/api/v1/games/rocom/merchant/info"
        val req = Request.Builder().url(url).apply {
            if (apiKey.isNotBlank()) header("X-API-Key", apiKey)
            header("User-Agent", "MagicFruitRadar/0.1 (Android)")
            header("Accept", "application/json")
        }.build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                throw Exception("HTTP " + resp.code + ": " + (resp.body?.string()?.take(200) ?: ""))
            }
            val body = resp.body?.string() ?: throw Exception("空响应")
            gson.fromJson(body, MerchantResponse::class.java)
        }
    }

    private suspend fun postIngameMerchantInfo(): MerchantResponse = kotlinx.coroutines.withContext(
        kotlinx.coroutines.Dispatchers.IO
    ) {
        val url = baseUrl.trimEnd('/') + "/api/v1/games/rocom/ingame/merchant/info"
        val jsonMedia = "application/json; charset=utf-8".toMediaType()
        val body = "{}".toRequestBody(jsonMedia)
        val req = Request.Builder().url(url).post(body).apply {
            if (apiKey.isNotBlank()) header("X-API-Key", apiKey)
            header("User-Agent", "MagicFruitRadar/0.1 (Android)")
            header("Accept", "application/json")
        }.build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                throw Exception("HTTP " + resp.code + ": " + (resp.body?.string()?.take(200) ?: ""))
            }
            val body = resp.body?.string() ?: throw Exception("空响应")
            gson.fromJson(body, MerchantResponse::class.java)
        }
    }
}
