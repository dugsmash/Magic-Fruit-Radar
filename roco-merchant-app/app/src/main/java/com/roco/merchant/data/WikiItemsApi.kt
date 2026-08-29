package com.roco.merchant.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/** 官方图鉴 API：按道具名搜索图标并下载缓存 */
class WikiItemsApi(private val baseUrl: String, private val apiKey: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    data class ItemHit(val name: String? = null, val icon: String? = null)

    data class ItemPage(
        val items: List<ItemHit>? = null,
        @com.google.gson.annotations.SerializedName("total") val total: Int? = null,
        @com.google.gson.annotations.SerializedName("total_pages") val totalPages: Int? = null
    )

    data class ItemData(val data: ItemPage? = null)

    /** 一页图鉴结果（含分页信息） */
    data class PageResult(val items: List<ItemHit>, val totalPages: Int?)

    /** 按名称搜索道具，返回第一个匹配的图标 URL（绝对地址或相对路径） */
    suspend fun searchIcon(name: String): String? =
        listItems(q = name, pageSize = 5).firstOrNull { it.icon != null }?.icon

    /**
     * 图鉴分页列表：q 为空返回全部图鉴（按页翻），q 非空按名称/描述搜索。
     * 返回 items 列表（含 name 与 icon 完整 URL）。
     */
    suspend fun listItems(q: String? = null, page: Int = 1, pageSize: Int = 100): List<ItemHit> =
        listItemsPage(q = q, page = page, pageSize = pageSize).items

    /** 图鉴分页列表（带 totalPages 用于进度显示） */
    suspend fun listItemsPage(q: String? = null, page: Int = 1, pageSize: Int = 100): PageResult =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val sb = StringBuilder(baseUrl.trimEnd('/'))
                    .append("/api/v1/games/rocom/wiki/items?page_no=").append(page)
                    .append("&page_size=").append(pageSize)
                if (!q.isNullOrBlank()) {
                    sb.append("&q=").append(URLEncoder.encode(q, "UTF-8"))
                }
                val req = Request.Builder().url(sb.toString())
                    .header("X-API-Key", apiKey)
                    .header("User-Agent", "RocoMerchant/0.1 (Android)")
                    .build()
                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@use PageResult(emptyList(), null)
                    val text = resp.body?.string() ?: return@use PageResult(emptyList(), null)
                    val pageData = gson.fromJson(text, ItemData::class.java)?.data
                    val items = pageData?.items?.filter { !it.name.isNullOrBlank() } ?: emptyList()
                    PageResult(items, pageData?.totalPages)
                }
            } catch (e: Exception) { PageResult(emptyList(), null) }
        }

    /** 下载图标到缓存目录：返回本地文件（失败返回 null） */
    suspend fun downloadIcon(iconPath: String, cacheFile: File): File? = kotlinx.coroutines.withContext(
        kotlinx.coroutines.Dispatchers.IO
    ) {
        try {
            // 图鉴 API 返回的 icon 可能是完整 URL，也可能是相对路径；统一处理
            val url = if (iconPath.startsWith("http://") || iconPath.startsWith("https://"))
                iconPath
            else
                baseUrl.trimEnd('/') + iconPath
            val req = Request.Builder().url(url)
                .header("X-API-Key", apiKey)
                .header("User-Agent", "RocoMerchant/0.1 (Android)")
                .build()
            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return@use null
                val bytes = resp.body?.bytes() ?: return@use null
                if (bytes.size < 100) return@use null
                cacheFile.writeBytes(bytes)
                cacheFile
            }
        } catch (e: Exception) { null }
    }

    companion object {
        /** 图标缓存目录 */
        fun iconDir(context: Context): File =
            File(context.filesDir, "item_icons").apply { mkdirs() }

        fun cacheFile(context: Context, name: String): File =
            File(iconDir(context), sanitize(name) + ".png")

        fun sanitize(name: String): String = name.replace(Regex("[^\\w\\u4e00-\\u9fa5]"), "_")

        /** 内置图鉴图标在 assets 中的路径 */
        fun assetIconPath(name: String): String = "items/" + sanitize(name) + ".png"

        /** 读取图标：优先本地缓存 files/item_icons/，其次内置 assets/items/（返回 null 表示都没有） */
        fun loadIconBitmap(context: Context, name: String): Bitmap? {
            val cacheFile = cacheFile(context, name)
            if (cacheFile.exists()) {
                val b = BitmapFactory.decodeFile(cacheFile.absolutePath)
                if (b != null) return b
            }
            return try {
                context.assets.open(assetIconPath(name)).use { BitmapFactory.decodeStream(it) }
            } catch (e: Exception) { null }
        }

        /** 内置图标数量（assets/items/ 文件数） */
        fun bundledIconCount(context: Context): Int =
            try { context.assets.list("items")?.size ?: 0 } catch (e: Exception) { 0 }

        /** 该道具是否已有内置图标（assets/items/） */
        fun hasBundledIcon(context: Context, name: String): Boolean = try {
            context.assets.open(assetIconPath(name)).close()
            true
        } catch (e: Exception) { false }
    }
}
