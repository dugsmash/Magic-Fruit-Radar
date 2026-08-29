package com.roco.merchant.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.roco.merchant.data.Prefs

/**
 * 开机/升级/改时间/改时区后自动重排后台任务。
 *
 * 手机重启后系统会清除全部闹钟与调度任务（部分厂商 ROM 还会拦截 WorkManager
 * 内部的开机恢复），如果不重新排队，App 在用户再次打开前都无法后台检测。
 * 这里在关键系统事件后主动重排「本轮检测 + 周期守护」，保证重启后依旧能提醒。
 *
 * 用户可在设置页关闭「开机自启动」：关闭后此处直接取消全部后台任务（含
 * WorkManager 自身的开机恢复），实现彻底解除自启动。
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val rearm = when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> true
            else -> false
        }
        if (!rearm) return
        if (!Prefs(context).autoStart) {
            // 用户已解除自启动：取消全部后台任务，重启后不再自动恢复
            WorkScheduler.disableBackground(context)
            return
        }
        // 检测与调度策略都按北京时间计算，时间/时区变化后必须重算下一轮时刻
        WorkScheduler.schedule(context)
        WorkScheduler.ensureWatchdog(context)
    }
}
