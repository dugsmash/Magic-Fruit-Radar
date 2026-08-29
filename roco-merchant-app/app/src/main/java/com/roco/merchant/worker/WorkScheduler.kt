package com.roco.merchant.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.roco.merchant.data.MerchantPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * 后台调度：远行商人每轮（北京时间 08/12/16/20 点）刷新后 5 分钟自动检测一次。
 *
 * 三层保障，保证「手机锁屏/切后台/重启后」依然能提醒：
 * 1. 一次性精确任务（[schedule]）：到点执行检测，检测完自动排下一轮；
 * 2. 周期守护任务（[ensureWatchdog]）：每 30 分钟自查一次——该轮漏检就补检，
 *    一次性任务丢失就重新排期（进程被 ROM 清理等异常场景自愈）；
 * 3. 开机重排（BootReceiver）：重启/升级/改时区后立即恢复调度。
 */
object WorkScheduler {

    private const val CHECK_WORK = "merchant_check"
    private const val WATCHDOG_WORK = "merchant_watchdog"
    /** 守护周期：兼顾恢复速度（≤30 分钟）与耗电 */
    private const val WATCHDOG_INTERVAL_MIN = 30L

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** 排下一轮检测（到点执行一次；覆盖旧任务，保证时刻最新） */
    fun schedule(context: Context) {
        val delayMs = (MerchantPolicy.nextRoundFetchAt() - System.currentTimeMillis()).coerceAtLeast(60_000L)
        enqueueCheck(context, delayMs, ExistingWorkPolicy.REPLACE)
    }

    /** 立即执行一次检测（守护任务发现漏检时调用） */
    fun enqueueCheckNow(context: Context) {
        enqueueCheck(context, 0L, ExistingWorkPolicy.REPLACE)
    }

    private fun enqueueCheck(context: Context, delayMs: Long, policy: ExistingWorkPolicy) {
        val req = OneTimeWorkRequestBuilder<MerchantCheckWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setConstraints(networkConstraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(CHECK_WORK, policy, req)
    }

    /** 启动周期守护（幂等：已存在则不动） */
    fun ensureWatchdog(context: Context) {
        val req = PeriodicWorkRequestBuilder<WatchdogWorker>(WATCHDOG_INTERVAL_MIN, TimeUnit.MINUTES)
            .setInitialDelay(WATCHDOG_INTERVAL_MIN, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WATCHDOG_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            req
        )
    }

    /** 解除自启动：取消守护任务与排队中的检测任务（用户关闭「开机自启动」时调用） */
    fun disableBackground(context: Context) {
        val wm = WorkManager.getInstance(context)
        wm.cancelUniqueWork(WATCHDOG_WORK)
        wm.cancelUniqueWork(CHECK_WORK)
    }

    /** 是否还有排队中/运行中的一次性检测任务（用于守护任务判断是否丢任务） */
    suspend fun hasPendingCheck(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val infos = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(CHECK_WORK)
                .get(3, TimeUnit.SECONDS)
            infos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
        } catch (e: Exception) {
            true // 查询失败时保守认为已有任务，避免重复排队
        }
    }

    /** 下次自动检测时刻（北京时间），用于设置页展示 */
    fun nextCheckLabel(): String {
        val at = MerchantPolicy.nextRoundFetchAt()
        val fmt = java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.US)
        fmt.timeZone = java.util.TimeZone.getTimeZone("Asia/Shanghai")
        return fmt.format(java.util.Date(at)) + "（北京时间）"
    }

    /** 守护任务是否已注册（设置页状态展示用） */
    suspend fun watchdogActive(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val infos = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(WATCHDOG_WORK)
                .get(3, TimeUnit.SECONDS)
            infos.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}
