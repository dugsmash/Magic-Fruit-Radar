package com.roco.merchant.data

/** 《洛克王国：世界》远行商人：固定稀有商品与默认愿望 */
object GoodsCatalog {

    /** 固定稀有商品（不可变更） */
    val RARE_NAMES: Set<String> = setOf("棱镜球", "祝福项链", "炫彩精灵蛋")

    /** 默认愿望单（与图鉴/商人名称保持一致） */
    val DEFAULT_WISH: List<String> = listOf("棱镜球", "祝福项坠", "炫彩精灵蛋", "首领血脉秘药")

    fun isRare(name: String): Boolean = name in RARE_NAMES

    /** 兜底图标（本地无图时） */
    fun emojiOf(name: String): String = when {
        name.contains("棱镜球") -> "🔮"
        name.contains("项链") || name.contains("项坠") -> "📿"
        name.contains("精灵蛋") || name.contains("蛋") -> "🥚"
        name.contains("血脉") || name.contains("秘药") || name.contains("凝露") || name.contains("药剂") -> "🧪"
        name.contains("魔镜") -> "🪞"
        name.contains("钥匙") -> "🔑"
        name.contains("球") -> "⚽"
        name.contains("果") -> "🍎"
        name.contains("矿石") || name.contains("琉璃") || name.contains("刚玉") -> "💎"
        name.contains("灵石") -> "💠"
        name.contains("粉尘") -> "🌫️"
        else -> "🎁"
    }
}
