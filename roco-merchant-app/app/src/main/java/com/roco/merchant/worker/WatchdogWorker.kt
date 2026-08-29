package com.roco.merchant.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.roco.merchant.data.MerchantPolicy
import com.roco.merchant.data.Prefs

/**
 * 周期守护任务：每 30 分钟自查一次，自愈两类后台异常——
 *
 * 1. 该轮（08/12/16/20 点 +5 分钟）已到点但还没检测过 → 立即补检一次；
 * 2. 下一轮的一次性检测任务丢失（进程被 ROM 清理、任务异常取消等）→ 重新排期。
 *
 * 正常运行时（一次性任务在跑、轮次已检测）该任务只做轻量检查，不重复请求接口。
 */
class WatchdogWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext
        return try {
            val prefs = Prefs(app)
            if (prefs.apiKey.isNotBlank() && MerchantPolicy.autoFetchDue(prefs.lastFetchKey)) {
                // 该轮到点且尚未检测（一次性任务可能已被系统/ROM 清理）→ 立即补检
                WorkScheduler.enqueueCheckNow(app)
            } else if (!WorkScheduler.hasPendingCheck(app)) {
                // 下一轮任务不在队列里 → 重新排期
                WorkScheduler.schedule(app)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
