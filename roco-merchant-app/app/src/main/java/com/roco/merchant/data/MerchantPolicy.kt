package com.roco.merchant.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** 远行商人固定刷新窗口策略：每轮（北京时间 08/12/16/20 点开始）刷新后 5 分钟，最多自动请求一次 */
object MerchantPolicy {

    private val TZ = TimeZone.getTimeZone("Asia/Shanghai")

    fun currentRoundId(now: Long = System.currentTimeMillis()): String? {
        val cal = Calendar.getInstance(TZ).apply { timeInMillis = now }
        return when (cal.get(Calendar.HOUR_OF_DAY)) {
            in 8..11 -> "08"
            in 12..15 -> "12"
            in 16..19 -> "16"
            in 20..23 -> "20"
            else -> null
        }
    }

    fun currentKey(now: Long = System.currentTimeMillis()): String {
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.US).apply { timeZone = TZ }
        return fmt.format(Date(now)) + "/" + (currentRoundId(now) ?: "closed")
    }

    /** 北京时间日期 key */
    fun dateKey(now: Long = System.currentTimeMillis()): String {
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.US).apply { timeZone = TZ }
        return fmt.format(Date(now))
    }

    /** 北京时间前一天日期 key */
    fun yesterdayKey(now: Long = System.currentTimeMillis()): String {
        val cal = Calendar.getInstance(TZ).apply { timeInMillis = now }
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.US).apply { timeZone = TZ }
        return fmt.format(cal.time)
    }

    /** 北京时间当日 0 点（closed 轮次的开始） */
    fun dayStartMs(now: Long): Long {
        val cal = Calendar.getInstance(TZ).apply { timeInMillis = now }
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** 北京时间当日 24 点 */
    fun dayEndMs(now: Long): Long = dayStartMs(now) + 24 * 60 * 60 * 1000L

    /** 轮次结束时间：08/12/16 = 开始+4小时；20 = 当日 24 点 */
    fun roundEndMs(roundId: String, now: Long): Long =
        if (roundId == "20") dayEndMs(now) else roundStartMs(roundId, now) + 4 * 60 * 60 * 1000L

    /** 休市：北京时间 0-8 点（无售卖轮次） */
    fun isClosedHours(now: Long = System.currentTimeMillis()): Boolean =
        currentRoundId(now) == null

    fun roundStartMs(roundId: String, now: Long): Long {
        val cal = Calendar.getInstance(TZ).apply { timeInMillis = now }
        cal.set(Calendar.HOUR_OF_DAY, roundId.toInt())
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** 是否该自动请求：已进入新一轮且距轮开始过 5 分钟，且该轮还没请求过 */
    fun autoFetchDue(lastFetchKey: String, now: Long = System.currentTimeMillis()): Boolean {
        val round = currentRoundId(now) ?: return false
        val key = currentKey(now)
        if (key == lastFetchKey) return false
        return now >= roundStartMs(round, now) + 5 * 60 * 1000L
    }

    /** 下一次自动检测时刻（北京时间）：每轮 08/12/16/20 点开始后 5 分钟 */
    fun nextRoundFetchAt(now: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance(TZ).apply { timeInMillis = now }
        for (h in intArrayOf(8, 12, 16, 20)) {
            cal.set(Calendar.HOUR_OF_DAY, h)
            cal.set(Calendar.MINUTE, 5); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            if (cal.timeInMillis > now) return cal.timeInMillis
        }
        // 今天已过 20:05，排到明天 08:05
        cal.add(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, 8)
        cal.set(Calendar.MINUTE, 5); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

}
