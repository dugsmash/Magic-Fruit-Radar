package com.roco.merchant.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/** 每轮一次的检测任务：检查远行商人货架，并排好下一轮 */
class MerchantCheckWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        MerchantChecker.check(applicationContext)
        WorkScheduler.schedule(applicationContext) // 排队下一轮（刷新后 5 分钟）
        WorkScheduler.ensureWatchdog(applicationContext) // 保证自愈守护一直在（幂等）
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "merchant_check"
    }
}
