package com.roco.merchant.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** 图鉴同步：拉取全部道具清单 + 补全缺失图标（手动触发；图标为公共资源，不消耗积分） */
object AtlasRepository {

    const val PAGE_SIZE = 100
    private const val CONCURRENCY = 10
    private const val TAG = "AtlasSync"

    data class SyncProgress(
        val totalItems: Int,
        val downloaded: Int,
        val failed: Int,
        val error: String? = null,
        val phase: Int = 0,      // 0 = 拉取清单，1 = 补全图标
        val page: Int = 0,
        val totalPages: Int = 0,
        val needIcons: Int = 0
    )

    /**
     * 1) 分页拉取全部图鉴清单，每页后增量写入 atlas.json（可中断续传）
     *    —— 元数据接口有频率限制（约 1 次/2 秒），逐页间隔拉取，429 时退避重试
     * 2) 补全缺失图标到 files/item_icons/（内置 assets 已有的跳过；并发，公共资源不限频）
     * 返回进度摘要（已在 IO 线程执行）；失败时 error 字段非空。
     */
    suspend fun sync(ctx: Context, onProgress: (SyncProgress) -> Unit = {}): SyncProgress =
        withContext(Dispatchers.IO) {
            val prefs = Prefs(ctx)
            if (prefs.apiKey.isBlank()) {
                Log.w(TAG, "apiKey 未配置，跳过图鉴同步")
                return@withContext SyncProgress(0, 0, 0, "未配置 API Key：请先到「设置」填写洛克魔法书 Key 再同步")
            }
            try {
                val api = WikiItemsApi(prefs.baseUrl, prefs.apiKey)

                // 1) 清单：逐页拉取，每页后增量保存
                val collected = ArrayList<AtlasStore.AtlasItem>()
                var page = 1
                var totalPages = 0
                while (page <= 500) {
                    var res: WikiItemsApi.PageResult = WikiItemsApi.PageResult(emptyList(), null)
                    repeat(3) { attempt ->
                        res = api.listItemsPage(page = page, pageSize = PAGE_SIZE)
                        if (res.items.isNotEmpty() || attempt == 2) return@repeat
                        Log.w(TAG, "page " + page + " 第 " + (attempt + 1) + " 次为空，退避后重试")
                        delay(3000L * (attempt + 1)) // 429/失败退避
                    }
                    if (res.items.isEmpty()) {
                        Log.e(TAG, "page " + page + " 拉取失败（3 次尝试后为空），停止")
                        break
                    }
                    if (page == 1) totalPages = res.totalPages ?: 0
                    res.items.forEach { hit ->
                        val n = hit.name?.trim()
                        if (!n.isNullOrEmpty() && !hit.icon.isNullOrEmpty()) {
                            collected.add(AtlasStore.AtlasItem(n, hit.icon!!))
                        }
                    }
                    val merged = AtlasStore.merge(AtlasStore.load(ctx), collected)
                    AtlasStore.save(ctx, merged) // 增量落盘：中断后下次续传
                    Log.d(TAG, "page " + page + " 完成 · 累计收录 " + merged.size + " 件")
                    onProgress(SyncProgress(merged.size, 0, 0, null, 0, page, totalPages, 0))
                    if (res.items.size < PAGE_SIZE) break
                    page++
                    delay(1600L) // 元数据接口限频：每页间隔约 1.6s
                }
                if (collected.isEmpty()) {
                    return@withContext SyncProgress(0, 0, 0, "拉取图鉴清单失败：网络不可用或 Key 无效")
                }
                val all = AtlasStore.load(ctx)

                // 2) 图标：只补「本地缓存和内置 assets 都没有」的（并发下载）
                var downloaded = 0
                var failed = 0
                val need = all.filter {
                    it.icon.isNotEmpty() &&
                    !WikiItemsApi.cacheFile(ctx, it.name).exists() &&
                    !WikiItemsApi.hasBundledIcon(ctx, it.name)
                }
                Log.d(TAG, "清单 " + all.size + " 件 · 待补图标 " + need.size + " 张（内置已覆盖的不重复下载）")
                coroutineScope {
                    need.chunked(CONCURRENCY).forEach { chunk ->
                        val results = chunk.map { item ->
                            async {
                                try {
                                    api.downloadIcon(item.icon, WikiItemsApi.cacheFile(ctx, item.name)) != null
                                } catch (e: Exception) { false }
                            }
                        }.awaitAll()
                        results.forEach { ok -> if (ok) downloaded++ else failed++ }
                        onProgress(SyncProgress(all.size, downloaded, failed, null, 1, totalPages, totalPages, need.size))
                    }
                }
                Log.d(TAG, "图标补全完成：成功 " + downloaded + " · 失败 " + failed)
                SyncProgress(all.size, downloaded, failed)
            } catch (e: Exception) {
                Log.e(TAG, "图鉴同步异常", e)
                SyncProgress(0, 0, 0, "同步失败：" + (e.message ?: e.toString()))
            }
        }
}
