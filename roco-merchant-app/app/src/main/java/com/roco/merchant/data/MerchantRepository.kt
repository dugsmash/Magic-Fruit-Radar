package com.roco.merchant.data

/** 拉取货架 → 展平 → 过滤 → 匹配许愿单 */
object MerchantRepository {

    fun flattenProducts(res: MerchantResponse): List<ProductItem> {
        val out = LinkedHashMap<String, ProductItem>()
        for (act in res.activities) {
            for (list in listOf(act.products, act.productList, act.getProps, act.getExtraProps, act.getPets)) {
                list?.forEach { p -> if (!p.name.isNullOrBlank()) out.putIfAbsent(p.name!!, p) }
            }
        }
        // random_goods 兜底（goods_name / name / price / buy_limit_num 结构）
        for (r in res.randoms) {
            val nm = r.displayName
            if (nm.isNotBlank()) {
                out.putIfAbsent(
                    nm,
                    ProductItem(
                        name = nm,
                        price = r.price ?: r.originPrice ?: r.buyLimitNum ?: r.limit,
                        num = r.itemNum ?: r.num,
                        startTime = r.startTime,
                        endTime = r.endTime,
                        iconUrl = r.iconUrl
                    )
                )
            }
        }
        return out.values.toList()
    }

    fun isActive(item: ProductItem, now: Long = System.currentTimeMillis()): Boolean {
        val start = normalizeTs(item.startTime)
        val end = normalizeTs(item.endTime)
        return (start == null || now >= start) && (end == null || now < end)
    }

    private fun normalizeTs(v: Any?): Long? {
        if (v == null) return null
        return try {
            val n = (v as? Number)?.toLong() ?: v.toString().toLong()
            if (n < 100_000_000_000L) n * 1000L else n
        } catch (e: Exception) { null }
    }

    /** 匹配许愿单（支持包含匹配） */
    fun matchWishlist(products: List<ProductItem>, wishes: List<WishItem>): List<ProductItem> {
        val enabled = wishes.filter { it.enabled }.map { it.name.trim() }.filter { it.isNotBlank() }
        if (enabled.isEmpty()) return emptyList()
        return products.filter { p ->
            enabled.any { w -> p.displayName.contains(w) || w.contains(p.displayName) }
        }
    }
}
