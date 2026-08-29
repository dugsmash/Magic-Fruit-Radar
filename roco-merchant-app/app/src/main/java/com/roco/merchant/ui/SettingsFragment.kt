package com.roco.merchant.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roco.merchant.data.AtlasRepository
import com.roco.merchant.data.AtlasStore
import com.roco.merchant.data.Prefs
import com.roco.merchant.data.WikiItemsApi
import com.roco.merchant.databinding.FragmentSettingsBinding
import com.roco.merchant.notify.Notifier
import com.roco.merchant.util.BatteryOptimizer
import com.roco.merchant.worker.MerchantChecker
import com.roco.merchant.worker.WorkScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: Prefs

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        prefs = Prefs(requireContext())
        loadValues()

        binding.getKeyLink.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://rocom.shallow.ink/")))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "无法打开浏览器：" + (e.message ?: ""), Toast.LENGTH_SHORT).show()
            }
        }

        binding.saveBtn.setOnClickListener {
            prefs.apiKey = binding.apiKeyInput.text?.toString()?.trim() ?: ""
            prefs.baseUrl = binding.baseUrlInput.text?.toString()?.trim() ?: ""
            prefs.classMode = binding.classSwitch.isChecked
            if (prefs.autoStart) WorkScheduler.schedule(requireContext())
            Toast.makeText(requireContext(), "设置已保存，后台任务已调度", Toast.LENGTH_SHORT).show()
            maybeGuideIconDownload()
        }

        binding.testBtn.setOnClickListener {
            val classMode = binding.classSwitch.isChecked
            Notifier(requireContext()).test(classMode)
            Toast.makeText(
                requireContext(),
                if (classMode) "已按免打扰模式发送测试通知（静音+不震动，请下拉通知栏查看）"
                else "已发送测试通知（声音+震动，请下拉通知栏查看）",
                Toast.LENGTH_LONG
            ).show()
        }

        // 后台运行保护：电池优化白名单 + 自启动开关 + 各品牌后台白名单指引
        binding.autoStartSwitch.isChecked = prefs.autoStart
        binding.autoStartSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.autoStart = checked
            if (checked) {
                WorkScheduler.schedule(requireContext())
                WorkScheduler.ensureWatchdog(requireContext())
                Toast.makeText(requireContext(), "已开启自启动：重启后自动恢复后台提醒", Toast.LENGTH_SHORT).show()
            } else {
                WorkScheduler.disableBackground(requireContext())
                Toast.makeText(requireContext(), "已解除自启动：重启不再自动恢复，后台守护已停止", Toast.LENGTH_SHORT).show()
            }
            refreshBgStatus()
        }
        binding.btnBatteryExempt.setOnClickListener {
            val ok = BatteryOptimizer.request(requireContext())
            Toast.makeText(
                requireContext(),
                if (ok) "已打开电池优化设置，请允许「忽略电池优化」（返回后状态会自动刷新）"
                else "未能自动打开，请在系统设置 → 应用 → 远行商人闹钟 → 电池中允许后台运行",
                Toast.LENGTH_LONG
            ).show()
        }
        binding.btnVendorGuide.setOnClickListener { showVendorGuide() }
        refreshBgStatus()

        // 引用来源说明
        binding.btnCredits.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("📚 数据与图片来源")
                .setMessage(
                    "商品数据\n" +
                    "· 洛克魔法书开放 API（wegame.shallow.ink，Key 获取：rocom.shallow.ink）\n" +
                    "  - 商人货架：GET /api/v1/games/rocom/merchant/info\n\n" +
                    "道具图鉴与图片\n" +
                    "· 洛克魔法书 Wiki 图鉴 API（/api/v1/games/rocom/wiki/items）\n" +
                    "· 图标原图：/api/v1/resources/wiki/assets/items/bag/<id>.png（公共资源，不消耗积分）\n\n" +
                    "说明：本应用内置全部道具名称图鉴（离线可用）；道具图标需联网下载\n" +
                    "（设置页「🔄 立即同步全部图鉴与图片」，约 2 分钟，不消耗积分）。\n" +
                    "图标下载后保存在手机本地，软件更新不会删除；未下载的图标以名称 + emoji 显示。\n" +
                    "远行商人商品随轮次动态变化，货架显示以游戏内实时数据为准。"
                )
                .setPositiveButton("知道了", null)
                .show()
        }

        // 图鉴同步（全量拉取 + 补全图片，进度条 + 文本实时显示）
        binding.btnAtlasSync.setOnClickListener { runAtlasSync() }
    }

    /** 图鉴同步：全量拉取道具名称清单 + 补全缺失图标（公共资源，不消耗积分） */
    private fun runAtlasSync() {
        if (_binding == null) return
        binding.btnAtlasSync.isEnabled = false
        binding.atlasProgress.progress = 0
        binding.atlasStatus.text = "正在同步全部图鉴…（元数据接口限频，需约 2 分钟）"
        lifecycleScope.launch {
            val p = AtlasRepository.sync(requireContext()) { prog ->
                if (_binding != null && isAdded) {
                    requireActivity().runOnUiThread {
                        val pct = progressPercent(prog)
                        binding.atlasProgress.progress = pct
                        binding.atlasStatus.text = if (prog.phase == 1)
                            "同步中… 图标补全 " + prog.downloaded + "/" + prog.needIcons + " 张（" + pct + "%）"
                        else
                            "同步中… 拉取图鉴清单 第 " + prog.page + "/" + prog.totalPages + " 页 · 已收录 " + prog.totalItems + " 件（" + pct + "%）"
                    }
                }
            }
            if (_binding != null) {
                binding.btnAtlasSync.isEnabled = true
                binding.atlasProgress.progress = if (p.error != null) 0 else 100
                binding.atlasStatus.text = if (p.error != null) "⚠️ " + p.error
                    else "图鉴同步完成：共 " + p.totalItems + " 件 · 补全图标 " + p.downloaded + " 张（失败 " + p.failed + "）"
            }
            val msg = if (p.error != null) p.error
                else "图鉴同步完成：共 " + p.totalItems + " 件，补全图标 " + p.downloaded + " 张"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 首次填入正确 API 后的图标下载引导（仅提示一次）：
     * 校验 Key 有效（拉取 Wiki 图鉴首页 1 条，公共接口不消耗积分）→ 弹窗告知图标需在设置中下载。
     */
    private fun maybeGuideIconDownload() {
        if (prefs.iconGuideShown) return
        val key = prefs.apiKey
        if (key.isBlank()) return
        lifecycleScope.launch {
            val valid = WikiItemsApi.validateKey(prefs.baseUrl, key)
            if (!valid || prefs.iconGuideShown) return@launch
            prefs.iconGuideShown = true
            if (_binding == null || !isAdded) return@launch
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("🎨 道具图标下载")
                .setMessage(
                    "安装包仅内置全部道具名称图鉴（离线可用），不包含道具图标。\n\n" +
                    "请联网下载全部道具图标：\n" +
                    "设置 →「🔄 立即同步全部图鉴与图片」（公共资源，不消耗积分，约 2 分钟）。\n\n" +
                    "图标下载后保存在手机本地，软件更新不会删除；\n" +
                    "未下载的图标将以道具名称 + emoji 显示。"
                )
                .setPositiveButton("立即下载图标") { _, _ -> runAtlasSync() }
                .setNegativeButton("稍后再说", null)
                .show()
        }
    }

    /** 同步进度 → 百分比：拉取清单占 85%，补全图标占 15% */
    private fun progressPercent(p: com.roco.merchant.data.AtlasRepository.SyncProgress): Int = when {
        p.error != null -> 0
        p.phase == 1 -> if (p.needIcons > 0) (85 + p.downloaded * 15 / p.needIcons).coerceIn(0, 100) else 100
        p.totalPages > 0 -> (p.page * 85 / p.totalPages).coerceIn(0, 85)
        else -> 0
    }

    /** 刷新「后台运行保护」状态：自启动 + 电池白名单 + 下次检测时刻 + 守护任务 */
    private fun refreshBgStatus() {
        if (_binding == null) return
        val ctx = requireContext()
        val auto = if (prefs.autoStart) "✅ 已开启" else "已解除（重启不自动恢复）"
        val battery = if (BatteryOptimizer.isIgnoring(ctx)) "✅ 已忽略电池优化"
        else "⚠️ 未忽略电池优化（锁屏可能冻结后台检测）"
        val next = WorkScheduler.nextCheckLabel()
        binding.bgStatus.text = "自启动：$auto\n电池优化：$battery\n下次自动检测：$next\n守护任务：检查中…"
        lifecycleScope.launch {
            val ok = WorkScheduler.watchdogActive(ctx)
            if (_binding != null && isAdded) {
                binding.bgStatus.text =
                    "自启动：$auto\n电池优化：$battery\n下次自动检测：$next\n守护任务：" + if (ok) "运行中 ✅（每 30 分钟自检，漏检自动补）" else "未注册（已解除自启动）"
            }
        }
    }

    /** 各品牌 ROM 后台白名单指引（保证锁屏/切后台后检测与提醒可靠） */
    private fun showVendorGuide() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("📖 各品牌后台白名单指引")
            .setMessage(
                "手机厂商省电策略会在锁屏/切后台后冻结 App，收不到到货提醒时按品牌操作：\n\n" +
                "【小米 / Redmi（HyperOS·MIUI）】\n" +
                "设置 → 应用设置 → 应用管理 → 远行商人闹钟 → 省电策略 → 无限制；\n" +
                "应用信息页开启「自启动」；最近任务里下拉锁定卡片。\n\n" +
                "【华为 / 荣耀（HarmonyOS·EMUI·MagicOS）】\n" +
                "设置 → 应用 → 应用启动管理 → 远行商人闹钟 → 手动管理 → 允许自启动/后台活动。\n\n" +
                "【OPPO / 一加（ColorOS）】\n" +
                "设置 → 应用管理 → 远行商人闹钟 → 允许自启动 + 允许后台运行；\n" +
                "电池 → 更多设置 → 允许后台运行/不受限制；最近任务下拉锁定。\n\n" +
                "【vivo / iQOO（OriginOS）】\n" +
                "设置 → 应用与权限 → 权限管理 → 自启动 → 允许；\n" +
                "i管家 → 应用管理 → 后台耗电管理 → 允许后台高耗电。\n\n" +
                "【三星（One UI）】\n" +
                "设置 → 应用 → 远行商人闹钟 → 电池 → 不受限制。\n\n" +
                "【原生 Android】\n" +
                "设置 → 应用 → 远行商人闹钟 → 电池 → 不受限制（本页「忽略电池优化」按钮即为快捷入口）。\n\n" +
                "完成后回到本页：电池优化显示 ✅、守护任务显示运行中，即可放心后台使用。\n\n" +
                "———— 解除自启动 / 后台权限（不需要时按品牌关闭）————\n" +
                "· 小米/Redmi：应用信息 → 自启动 → 关闭；省电策略 → 智能限制（默认）\n" +
                "· 华为/荣耀：应用启动管理 → 手动管理 → 关闭「自启动 / 后台活动」\n" +
                "· OPPO/一加：应用管理 → 关闭「自启动」「允许后台运行」\n" +
                "· vivo/iQOO：权限管理 → 自启动 → 禁止；i管家 → 后台耗电 → 智能限制\n" +
                "· 三星：应用 → 电池 → 优化（受限制）\n" +
                "· 原生：应用 → 电池 → 优化（移除「不受限制」）\n\n" +
                "更省事的做法：直接关闭本页「开机自启动」开关，重启后 App 不会自动恢复检测，" +
                "系统后台任务会一并取消（手动打开 App 仍可正常检测）。"
            )
            .setPositiveButton("知道了", null)
            .show()
    }

    private fun loadValues() {
        binding.apiKeyInput.setText(prefs.apiKey)
        binding.baseUrlInput.setText(prefs.baseUrl)
        binding.classSwitch.isChecked = prefs.classMode
        binding.autoStartSwitch.isChecked = prefs.autoStart
        binding.settingsInfo.text = "后端：" + prefs.baseUrl + "\nAPI Key：" +
            (if (prefs.apiKey.isBlank()) "未配置" else prefs.apiKey.take(6) + "…" + prefs.apiKey.takeLast(4)) +
            "\n策略：商人每轮(08/12/16/20点)刷新后 5 分钟自动检测一次" +
            "\n图鉴：已内置全部道具名称（离线），图标需联网同步下载" +
            "\n上次请求轮次：" + (if (prefs.lastFetchKey.isBlank()) "无" else prefs.lastFetchKey)
        // 图鉴统计：道具名称数（内置 atlas.json）+ 本地已下载图标数（files/item_icons/）
        val count = AtlasStore.load(requireContext()).size
        val icons = WikiItemsApi.cachedIconCount(requireContext())
        binding.atlasStatus.text = "已收录 " + count + " 件道具 · 本地图标 " + icons + " 张（未下载时显示名称 + emoji）"
    }

    override fun onResume() {
        super.onResume()
        if (::prefs.isInitialized) {
            loadValues()
            refreshBgStatus() // 从系统电池设置页返回后刷新白名单状态
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
