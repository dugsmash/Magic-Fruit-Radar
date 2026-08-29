package com.roco.merchant.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

/**
 * 电池优化白名单工具。
 *
 * Android 6+ 的 Doze 省电模式会冻结后台 App 的定时任务与网络，导致检测延迟
 * 甚至完全不执行。把本应用加入「忽略电池优化」白名单后，后台检测基本不受 Doze
 * 限制；国产 ROM（MIUI/EMUI/ColorOS 等）另有系统级白名单，见设置页指引。
 */
object BatteryOptimizer {

    /** 是否已处于「忽略电池优化」白名单 */
    fun isIgnoring(context: Context): Boolean {
        return try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isIgnoringBatteryOptimizations(context.packageName)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 发起申请：优先走系统「忽略电池优化」授权页；部分 ROM 不支持时退回
     * 电池优化设置列表。返回是否成功拉起系统页面。
     */
    fun request(context: Context): Boolean {
        return try {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:" + context.packageName)
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(fallback)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }
}
