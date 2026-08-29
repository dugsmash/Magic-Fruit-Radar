package com.roco.merchant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roco.merchant.data.AtlasRepository
import com.roco.merchant.data.Prefs
import com.roco.merchant.databinding.ActivityMainBinding
import com.roco.merchant.ui.CatalogFragment
import com.roco.merchant.ui.RecordFragment
import com.roco.merchant.ui.SettingsFragment
import com.roco.merchant.ui.WishlistFragment
import com.roco.merchant.util.BatteryOptimizer
import com.roco.merchant.worker.WorkScheduler
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val wishFragment = WishlistFragment()
    private val catalogFragment = CatalogFragment()
    private val recordFragment = RecordFragment()
    private val settingsFragment = SettingsFragment()

    private val notifPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* 结果忽略，未授权则不推送 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Android 13+ 通知权限
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (savedInstanceState == null) {
            showFragment(wishFragment)
        }
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_wish -> showFragment(wishFragment)
                R.id.nav_catalog -> showFragment(catalogFragment)
                R.id.nav_record -> showFragment(recordFragment)
                R.id.nav_settings -> showFragment(settingsFragment)
            }
            true
        }

        // 后台任务：商人每轮刷新后 5 分钟检测一次（图鉴已内置，可手动补全）
        // 受「开机自启动」开关控制：关闭后不调度、不守护、不引导（彻底解除后台提醒）
        val prefs = Prefs(this)
        if (prefs.autoStart) {
            WorkScheduler.schedule(this)
            // 周期守护：一次性任务丢失/漏检时自愈，保证锁屏、切后台后仍能提醒
            WorkScheduler.ensureWatchdog(this)
            // 首次运行引导：申请「忽略电池优化」白名单（Doze 冻结后台任务的直接对策）
            maybeGuideBatteryOptimization()
        } else {
            // 用户已解除自启动：再次打开 App 也保持关闭，并清理残留任务
            WorkScheduler.disableBackground(this)
        }

        // 调试触发：adb shell am start -n com.roco.merchant/.MainActivity --ez atlas_sync true
        val debuggable = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (debuggable && intent?.getBooleanExtra("atlas_sync", false) == true) {
            Toast.makeText(this, "图鉴同步开始：拉取全部道具并补全图标…", Toast.LENGTH_LONG).show()
            lifecycleScope.launch {
                val p = AtlasRepository.sync(applicationContext)
                Toast.makeText(this@MainActivity,
                    "图鉴同步完成：共 " + p.totalItems + " 件，补全图标 " + p.downloaded + " 张（失败 " + p.failed + "）",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showFragment(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, f)
            .commitAllowingStateLoss()
    }

    /** 首次运行引导：未加入电池优化白名单时，弹窗引导用户去开启（保证后台检测可靠） */
    private fun maybeGuideBatteryOptimization() {
        val prefs = Prefs(this)
        if (prefs.batteryGuideShown) return
        prefs.batteryGuideShown = true
        if (BatteryOptimizer.isIgnoring(this)) return
        MaterialAlertDialogBuilder(this)
            .setTitle("🔋 开启后台保护")
            .setMessage(
                "为了在锁屏 / 切到其他应用时仍能准时提醒「愿望商品到货」，\n\n" +
                "请允许本应用「忽略电池优化」。\n\n" +
                "若手机是小米/华为/OPPO/vivo 等国产系统，还建议在系统设置中" +
                "允许自启动并把应用加入后台白名单（设置页有各品牌指引）。"
            )
            .setPositiveButton("去设置") { _, _ -> BatteryOptimizer.request(this) }
            .setNegativeButton("暂不", null)
            .show()
    }
}
