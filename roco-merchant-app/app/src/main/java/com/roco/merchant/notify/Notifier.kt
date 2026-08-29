package com.roco.merchant.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.roco.merchant.R

/** 到货通知：消息 + 声音 + 震动（免打扰模式静默） */
class Notifier(private val ctx: Context) {

    companion object {
        const val CHANNEL_ID = "merchant_alerts"
        const val NOTIF_ID = 1001
    }

    init { ensureChannel() }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = ctx.getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        val ch = NotificationChannel(
            CHANNEL_ID,
            ctx.getString(R.string.notif_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = ctx.getString(R.string.notif_channel_desc)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 150, 300)
            setSound(Uri.parse("content://settings/system/notification_sound"), AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build())
        }
        nm.createNotificationChannel(ch)
    }

    private fun baseBuilder(title: String, text: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_wish)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
    }

    /** 发到货通知；classMode 时静音不震动 */
    fun notifyArrival(title: String, text: String, sound: Boolean, vibrate: Boolean, classMode: Boolean) {
        ensureChannel()
        if (classMode) {
            val silent = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_wish)
                .setContentTitle(title)
                .setContentText(text + "（免打扰模式：已静音）")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSilent(true)
                .setAutoCancel(true)
            notifyCompat(NOTIF_ID, silent)
            return
        }
        val b = baseBuilder(title, text)
        if (vibrate) b.setVibrate(longArrayOf(0, 300, 150, 300))
        if (sound) b.setSound(Uri.parse("content://settings/system/notification_sound"))
        notifyCompat(NOTIF_ID, b)
    }

    /** 按免打扰开关发测试通知：classMode 静默；否则声音+震动（默认固定开启） */
    fun test(classMode: Boolean) {
        val mode = if (classMode) "免打扰模式（已静音）" else "声音+震动"
        notifyArrival("远行商人 · 测试通知", "当前设置：$mode。愿望商品出现时会这样提醒你！", true, true, classMode)
    }

    private fun notifyCompat(id: Int, builder: NotificationCompat.Builder) {
        try {
            if (Build.VERSION.SDK_INT >= 33 &&
                ctx.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) return
            NotificationManagerCompat.from(ctx).notify(id, builder.build())
        } catch (e: Exception) { /* 忽略权限/服务异常 */ }
    }
}
