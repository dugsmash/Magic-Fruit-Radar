package com.roco.merchant.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Prefs(context: Context) {
    private val sp: SharedPreferences =
        context.getSharedPreferences("roco_merchant", Context.MODE_PRIVATE)
    private val gson = Gson()

    var apiKey: String
        get() = sp.getString("api_key", "") ?: ""
        set(v) = sp.edit().putString("api_key", v).apply()

    var baseUrl: String
        get() = sp.getString("base_url", "https://wegame.shallow.ink")!!
        set(v) = sp.edit().putString("base_url", v.trimEnd('/')).apply()

    /** 到货声音：固定开启（不再提供开关，由系统通知音决定） */
    val soundOn: Boolean get() = true

    /** 到货震动：固定开启（不再提供开关） */
    val vibrateOn: Boolean get() = true

    var classMode: Boolean
        get() = sp.getBoolean("class_mode", false)
        set(v) = sp.edit().putBoolean("class_mode", v).apply()

    /** 是否已展示过「后台保护」首次引导（避免每次启动都弹窗） */
    var batteryGuideShown: Boolean
        get() = sp.getBoolean("battery_guide_shown", false)
        set(v) = sp.edit().putBoolean("battery_guide_shown", v).apply()

    /** 开机自启动 + 后台守护总开关（默认开；关闭后重启不自动恢复检测、守护任务停止） */
    var autoStart: Boolean
        get() = sp.getBoolean("auto_start", true)
        set(v) = sp.edit().putBoolean("auto_start", v).apply()

    /** 许愿单（商品名列表） */
    fun getWishlist(): MutableList<WishItem> {
        val json = sp.getString("wishlist", null) ?: return mutableListOf()
        return try {
            val list: List<WishItem>? = gson.fromJson(json, object : TypeToken<List<WishItem>>() {}.type)
            (list ?: emptyList()).toMutableList()
        } catch (e: Exception) { mutableListOf() }
    }

    fun setWishlist(list: List<WishItem>) {
        sp.edit().putString("wishlist", gson.toJson(list)).apply()
    }

    fun addWish(name: String): Boolean {
        val list = getWishlist()
        if (list.any { it.name == name }) return false
        list.add(WishItem(name))
        setWishlist(list)
        return true
    }

    fun removeWish(name: String) {
        setWishlist(getWishlist().filter { it.name != name })
    }

    fun setWishEnabled(name: String, enabled: Boolean) {
        val list = getWishlist().map { if (it.name == name) it.copy(enabled = enabled) else it }
        setWishlist(list)
    }

    /** 上次成功请求的轮次 key（yyyyMMdd/轮次），用于每轮限一次自动请求 */
    var lastFetchKey: String
        get() = sp.getString("last_fetch_key", "") ?: ""
        set(v) = sp.edit().putString("last_fetch_key", v).apply()

    /** 缓存货架数据（避免重复请求烧积分） */
    fun getCachedProducts(): List<ProductItem> {
        val json = sp.getString("cached_products", null) ?: return emptyList()
        return try {
            val list: List<ProductItem>? = gson.fromJson(json, object : TypeToken<List<ProductItem>>() {}.type)
            list ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    fun setCachedProducts(list: List<ProductItem>) {
        sp.edit().putString("cached_products", gson.toJson(list)).apply()
    }

    /** 售卖记录（从首次成功获取商人数据开始记录） */
    fun getRecords(): MutableList<MerchantRecord> {
        val json = sp.getString("records", null) ?: return mutableListOf()
        return try {
            val list: List<MerchantRecord>? = gson.fromJson(json, object : TypeToken<List<MerchantRecord>>() {}.type)
            (list ?: emptyList()).toMutableList()
        } catch (e: Exception) { mutableListOf() }
    }

    fun setRecords(list: List<MerchantRecord>) {
        sp.edit().putString("records", gson.toJson(list)).apply()
    }

    /** 新增记录：同轮次去重，保留最近 90 条 */
    fun addRecord(rec: MerchantRecord) {
        val list = getRecords().filter { it.dateKey != rec.dateKey || it.roundId != rec.roundId }.toMutableList()
        list.add(rec)
        if (list.size > 90) list.removeAt(0)
        setRecords(list)
    }

    /** 许愿星（经验）与等级 */
    var stars: Long
        get() = sp.getLong("stars", 0)
        set(v) = sp.edit().putLong("stars", v).apply()

    /** 当前等级（由许愿星推导）：升到下一级所需 = 3 + (等级-1)*2 */
    fun levelFromStars(s: Long = stars): Int {
        var lvl = 1
        var need = 3L
        var remain = s
        while (remain >= need) { remain -= need; lvl++; need += 2 }
        return lvl
    }

    fun nextLevelNeed(level: Int): Long = 3 + (level - 1) * 2L

    fun starsInLevel(s: Long = stars): Long {
        var lvl = 1
        var need = 3L
        var remain = s
        while (remain >= need) { remain -= need; lvl++; need += 2 }
        return remain
    }

    /** 已确认抢到标记（dateKey/roundId/item），用于倒计时二次提醒与加星去重 */
    fun getClaimed(): MutableSet<String> {
        val json = sp.getString("claimed", null) ?: return mutableSetOf()
        return try {
            val set: Set<String>? = gson.fromJson(json, object : TypeToken<Set<String>>() {}.type)
            set?.toMutableSet() ?: mutableSetOf()
        } catch (e: Exception) { mutableSetOf() }
    }

    fun setClaimed(set: Set<String>) {
        sp.edit().putString("claimed", gson.toJson(set)).apply()
    }

    /** 初始化默认愿望单（首次启动） */
    fun ensureDefaultWish() {
        if (getWishlist().isEmpty()) {
            setWishlist(GoodsCatalog.DEFAULT_WISH.map { WishItem(it) })
        }
    }

    /** 已通知去重：name -> epochMillis，避免重复轰炸 */
    fun getNotified(): MutableMap<String, Long> {
        val json = sp.getString("notified", null) ?: return mutableMapOf()
        return try {
            val map: Map<String, Long>? = gson.fromJson(json, object : TypeToken<Map<String, Long>>() {}.type)
            map?.toMutableMap() ?: mutableMapOf()
        } catch (e: Exception) { mutableMapOf() }
    }

    fun setNotified(map: Map<String, Long>) {
        sp.edit().putString("notified", gson.toJson(map)).apply()
    }
}
