package com.roco.merchant.data

import com.google.gson.annotations.SerializedName

/** 远行商人接口响应（洛克魔法书/WeGame 后端）
 *  真实响应结构为 {code, message, data:{merchantActivities, random_goods}}，
 *  部分旧结构可能直接平铺顶层，两层均兼容。 */
data class MerchantResponse(
    @SerializedName("data") val data: MerchantData? = null,
    // 兼容无 data 包装的旧结构
    @SerializedName("merchantActivities") val merchantActivities: List<MerchantActivity>? = null,
    @SerializedName("merchant_activities") val merchantActivitiesSnake: List<MerchantActivity>? = null,
    @SerializedName("random_goods") val randomGoods: List<RandomGood>? = null,
    @SerializedName("randomGoods") val randomGoodsCamel: List<RandomGood>? = null
) {
    val activities: List<MerchantActivity>
        get() = (data?.merchantActivities ?: data?.merchantActivitiesSnake
            ?: merchantActivities ?: merchantActivitiesSnake ?: emptyList())
    val randoms: List<RandomGood>
        get() = (data?.randomGoods ?: data?.randomGoodsCamel
            ?: randomGoods ?: randomGoodsCamel ?: emptyList())
}

/** 响应 data 包装层 */
data class MerchantData(
    @SerializedName("merchantActivities") val merchantActivities: List<MerchantActivity>? = null,
    @SerializedName("merchant_activities") val merchantActivitiesSnake: List<MerchantActivity>? = null,
    @SerializedName("random_goods") val randomGoods: List<RandomGood>? = null,
    @SerializedName("randomGoods") val randomGoodsCamel: List<RandomGood>? = null
)

data class MerchantActivity(
    val products: List<ProductItem>? = null,
    @SerializedName("product_list") val productList: List<ProductItem>? = null,
    @SerializedName("get_props") val getProps: List<ProductItem>? = null,
    @SerializedName("get_extra_props") val getExtraProps: List<ProductItem>? = null,
    @SerializedName("get_pets") val getPets: List<ProductItem>? = null
)

data class RandomGood(
    val name: String? = null,
    @SerializedName("goods_name") val goodsName: String? = null,
    val price: Any? = null,
    @SerializedName("origin_price") val originPrice: Any? = null,
    val num: Any? = null,
    @SerializedName("item_num") val itemNum: Any? = null,
    val limit: Any? = null,
    @SerializedName("buy_limit_num") val buyLimitNum: Any? = null,
    @SerializedName("start_time") val startTime: Any? = null,
    @SerializedName("end_time") val endTime: Any? = null,
    @SerializedName("icon_url") val iconUrl: String? = null
) {
    /** 展示名：goods_name 优先，兼容 name */
    val displayName: String get() = goodsName ?: name ?: ""
}

/** 货架商品（统一模型） */
data class ProductItem(
    val name: String? = null,
    val price: Any? = null,
    val num: Any? = null,
    @SerializedName("start_time") val startTime: Any? = null,
    @SerializedName("end_time") val endTime: Any? = null,
    @SerializedName("icon_url") val iconUrl: String? = null,
    val image: String? = null,
    val id: Any? = null
) {
    val displayName: String get() = name ?: "未知商品"
    val displayPrice: String get() = price?.toString() ?: "?"
    val displayNum: String get() = num?.toString() ?: ""
}

/** 许愿条目 */
data class WishItem(val name: String, val enabled: Boolean = true, val addedAt: Long = System.currentTimeMillis())

/** 远行商人售卖记录：某时间段商人卖过什么 */
data class MerchantRecord(
    val dateKey: String,          // 北京时间 yyyyMMdd
    val roundId: String,          // 08/12/16/20/closed
    val startMs: Long,
    val endMs: Long,
    val items: List<String>,
    val fetchedAtMs: Long
) {
    val roundLabel: String
        get() = when (roundId) {
            "08" -> "08:00-12:00"
            "12" -> "12:00-16:00"
            "16" -> "16:00-20:00"
            "20" -> "20:00-24:00"
            else -> "非售卖时段"
        }
}

/** 一次检测的结果 */
data class CheckResult(
    val ok: Boolean,
    val message: String,
    val products: List<ProductItem> = emptyList(),
    val matches: List<ProductItem> = emptyList()
)
