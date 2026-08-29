package com.roco.merchant.worker

import android.content.Context
import com.roco.merchant.data.CheckResult
import com.roco.merchant.data.MerchantApi
import com.roco.merchant.data.MerchantPolicy
import com.roco.merchant.data.MerchantRecord
import com.roco.merchant.data.MerchantRepository
import com.roco.merchant.data.Prefs
import com.roco.merchant.data.ProductItem
import com.roco.merchant.notify.Notifier

/** 检测核心：拉取货架 → 匹配许愿单 → 发通知（带 30 分钟冷却去重）+ 售卖记录 */
object MerchantChecker {

    private const val COOLDOWN_MS = 30 * 60 * 1000L

    suspend fun check(ctx: Context, force: Boolean = false): CheckResult {
        val prefs = Prefs(ctx)
        if (prefs.apiKey.isBlank()) {
            return CheckResult(false, "未配置 API Key，请到「设置」填写（洛克魔法书获取）")
        }
        // 非手动触发：按商人刷新窗口限流，每轮最多自动请求一次，其余时间展示缓存
        if (!force && !MerchantPolicy.autoFetchDue(prefs.lastFetchKey)) {
            val cached = prefs.getCachedProducts()
            val msg = if (cached.isEmpty())
                "未到自动检测时间（商人 08/12/16/20 点刷新，每轮后 5 分钟检测一次）"
            else
                "商人本轮已检测，当前为缓存数据（" + cached.size + " 件）"
            return CheckResult(true, msg, cached)
        }
        return try {
            val api = MerchantApi(prefs.baseUrl, prefs.apiKey)
            val res = api.fetchMerchant()
            val products = MerchantRepository.flattenProducts(res)
            finalize(ctx, prefs, products, force, notifyOnForce = false)
        } catch (e: Exception) {
            CheckResult(false, "请求失败：" + (e.message ?: e.toString()))
        }
    }

    /** 免API模拟入口（测试用）：注入商品数据，走完整通知+记录链路 */
    fun checkWithProducts(
        ctx: Context,
        products: List<ProductItem>,
        notify: Boolean = true,
        markDateKey: String? = null
    ): CheckResult {
        val prefs = Prefs(ctx)
        return finalize(ctx, prefs, products, force = true, notifyOnForce = notify, markDateKey = markDateKey)
    }

    private fun finalize(
        ctx: Context,
        prefs: Prefs,
        products: List<ProductItem>,
        force: Boolean,
        notifyOnForce: Boolean,
        markDateKey: String? = null
    ): CheckResult {
        val now = System.currentTimeMillis()
        // 只保留当前时段（start_time~end_time 覆盖 now）在售商品，跨轮次商品不显示/不记录
        val active = products.filter { MerchantRepository.isActive(it, now) }
        // 记录本轮已请求 + 缓存货架（手动刷新也会更新，避免同轮重复自动请求）
        prefs.lastFetchKey = MerchantPolicy.currentKey()
        prefs.setCachedProducts(active)

        // 售卖记录：从首次成功获取数据开始，记录时间段与物品
        recordFetch(prefs, active, markDateKey, now)

        val matches = MerchantRepository.matchWishlist(active, prefs.getWishlist())

        if (!force || notifyOnForce) {
            val notified = prefs.getNotified()
            val fresh = matches.filter { m ->
                val last = notified[m.displayName] ?: 0L
                now - last > COOLDOWN_MS
            }
            if (fresh.isNotEmpty()) {
                val notifier = Notifier(ctx)
                fresh.forEach { m ->
                    var text = m.displayName + " 已出现在商人货架！价格 " + m.displayPrice
                    if (m.displayNum.isNotEmpty()) text += " · 数量 " + m.displayNum
                    notifier.notifyArrival(
                        "远行商人 · 商品到货",
                        text,
                        prefs.soundOn,
                        prefs.vibrateOn,
                        prefs.classMode
                    )
                    notified[m.displayName] = now
                }
                prefs.setNotified(notified)
            }
        }

        val msg = "检测完成：货架 " + active.size + " 件，命中许愿 " + matches.size + " 件"
        return CheckResult(true, msg, active, matches)
    }

    /** 记录本次轮次售卖物品（同轮次去重由 Prefs 处理） */
    private fun recordFetch(prefs: Prefs, products: List<ProductItem>, markDateKey: String?, now: Long) {
        val items = products.map { it.displayName }.distinct().take(30)
        if (items.isEmpty()) return
        val roundId = MerchantPolicy.currentRoundId(now) ?: "closed"
        val dateKey = markDateKey ?: MerchantPolicy.dateKey(now)
        val startMs = if (roundId == "closed") MerchantPolicy.dayStartMs(now)
            else MerchantPolicy.roundStartMs(roundId, now)
        val endMs = if (roundId == "closed") startMs + 8 * 60 * 60 * 1000L
            else MerchantPolicy.roundStartMs(roundId, now) + 4 * 60 * 60 * 1000L
        prefs.addRecord(MerchantRecord(dateKey, roundId, startMs, endMs, items, now))
    }
}
