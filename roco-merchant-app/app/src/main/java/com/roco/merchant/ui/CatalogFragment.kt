package com.roco.merchant.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.roco.merchant.R
import com.roco.merchant.data.GoodsCatalog
import com.roco.merchant.data.MerchantPolicy
import com.roco.merchant.data.Prefs
import com.roco.merchant.data.ProductItem
import com.roco.merchant.data.WikiItemsApi
import com.roco.merchant.databinding.FragmentCatalogBinding
import com.roco.merchant.notify.Notifier
import com.roco.merchant.worker.MerchantChecker
import kotlinx.coroutines.launch

/** 货架页：商品列表（洛克魔法书图鉴原图+名称+抢到）+ 倒计时 */
class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: Prefs
    private var products: List<ProductItem> = emptyList()
    private var currentShelfNames: List<String> = emptyList()
    private var countdownTicker: Thread? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        prefs = Prefs(requireContext())
        binding.refreshBtn.setOnClickListener {
            if (MerchantPolicy.isClosedHours()) {
                Toast.makeText(requireContext(), "休市中（0-8点）：8点后商人开始营业，将自动获取货架", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("手动刷新货架")
                .setMessage("手动刷新将调用付费接口（约 5 积分/次），可能产生额外费用。确定立即刷新吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定刷新") { _, _ -> fetch(force = true) }
                .show()
        }
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val kw = s?.toString()?.trim() ?: ""
                val all = if (products.isEmpty()) prefs.getCachedProducts() else products
                val names = all.map { it.displayName }.filter { kw.isEmpty() || it.contains(kw) }.distinct()
                renderList(names, if (kw.isEmpty()) "筛选：全部" else "筛选：「" + kw + "」")
            }
        })
    }

    private fun fetch(force: Boolean) {
        binding.catalogInfo.text = if (force) "正在手动刷新货架…" else "商人本轮已刷新，正在自动获取…"
        lifecycleScope.launch {
            val result = MerchantChecker.check(requireContext(), force = force)
            binding.catalogInfo.text = result.message
            if (result.products.isNotEmpty()) {
                products = result.products
                renderList(products.map { it.displayName }.distinct(), result.message)
            }
        }
    }

    private fun maybeAutoFetch() {
        if (MerchantPolicy.isClosedHours()) {
            val yesterdayItems = prefs.getRecords()
                .filter { it.dateKey == MerchantPolicy.yesterdayKey() }
                .flatMap { it.items }.distinct()
            renderList(
                yesterdayItems,
                if (yesterdayItems.isEmpty()) "休市中（0:00-8:00）· 暂无前一日货架记录"
                else "休市中 · 前一日货架回顾（" + yesterdayItems.size + " 件，不再售卖）"
            )
            updateCountdown()
            return
        }
        loadCached()
        if (MerchantPolicy.autoFetchDue(prefs.lastFetchKey)) {
            fetch(force = false)
        } else {
            val names = products.map { it.displayName }.distinct()
            renderList(
                names,
                if (names.isEmpty()) "尚未拉取过货架。商人每轮（08/12/16/20 点）刷新后自动获取一次"
                else "缓存货架 " + names.size + " 件"
            )
        }
        updateCountdown()
        maybeUrgentNotify()
    }

    private fun loadCached() {
        if (products.isEmpty()) products = prefs.getCachedProducts()
    }

    /** 商品列表：洛克魔法书图鉴原图 + 名称（渐变/红/黑）+ 抢到 */
    private fun renderList(names: List<String>, info: String) {
        binding.catalogInfo.text = info
        currentShelfNames = names.distinct()
        val container = binding.shelfContainer
        container.removeAllViews()
        if (currentShelfNames.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "暂无商品 🎁"
                textSize = 14f
                setTextColor(0xFF000000.toInt())
                gravity = Gravity.CENTER
                setPadding(0, 40, 0, 40)
            }
            container.addView(tv)
            return
        }
        val wishNames = prefs.getWishlist().filter { it.enabled }.map { it.name }.toSet()
        currentShelfNames.forEach { name ->
            container.addView(itemRow(name, name in wishNames))
        }
    }

    private fun itemRow(name: String, wishlisted: Boolean): LinearLayout {
        val rare = GoodsCatalog.isRare(name)
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundResource(R.drawable.bg_card)
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, 0, 0, dp(8))
        row.layoutParams = lp

        // 图标：本地缓存 → 洛克魔法书 Wiki 图鉴 API 异步下载 → emoji 兜底
        val iconView = ImageView(requireContext()).apply {
            adjustViewBounds = true
            layoutParams = LinearLayout.LayoutParams(dp(56), dp(56))
        }
        val cached = loadCachedIcon(name)
        if (cached != null) {
            iconView.setImageBitmap(cached)
        } else {
            iconView.setImageDrawable(null)
            val emoji = TextView(requireContext()).apply {
                text = GoodsCatalog.emojiOf(name)
                textSize = 34f
                layoutParams = LinearLayout.LayoutParams(dp(56), dp(56))
                gravity = Gravity.CENTER
            }
            row.addView(emoji)
            asyncLoadIcon(name) { bmp ->
                if (bmp != null) {
                    emoji.visibility = View.GONE
                    iconView.setImageBitmap(bmp)
                    row.removeView(emoji)
                    row.addView(iconView, 0)
                } else {
                    row.removeView(iconView)
                }
            }
        }
        if (cached != null) row.addView(iconView)

        val mid = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(dp(10), 0, dp(6), 0)
        }
        if (wishlisted) {
            mid.addView(GradientTextView(requireContext()).apply {
                text = name
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                maxLines = 1
                startFlow()
            })
        } else {
            mid.addView(TextView(requireContext()).apply {
                text = name
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
                setTextColor(if (rare) 0xFFD32F2F.toInt() else 0xFF000000.toInt())
            })
        }
        row.addView(mid)

        if (rare) {
            row.addView(TextView(requireContext()).apply {
                text = "🎯 抢到"
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(0xFFFFFFFF.toInt())
                gravity = Gravity.CENTER
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(0xFFE53935.toInt()); cornerRadius = dp(14).toFloat()
                }
                setPadding(dp(12), dp(7), dp(12), dp(7))
            }.also { btn -> btn.setOnClickListener { onClaim(name) } })
        }
        return row
    }

    /** 本地图标：内置 assets/items/ 优先，其次运行时缓存（手动补全下载） */
    private fun loadCachedIcon(name: String): Bitmap? =
        WikiItemsApi.loadIconBitmap(requireContext(), name)

    /** 官方图鉴按名搜图并下载缓存（异步） */
    private fun asyncLoadIcon(name: String, onDone: (Bitmap?) -> Unit) {
        lifecycleScope.launch {
            var bmp: Bitmap? = null
            try {
                val cacheFile = WikiItemsApi.cacheFile(requireContext(), name)
                if (!cacheFile.exists()) {
                    val api = WikiItemsApi(prefs.baseUrl, prefs.apiKey)
                    val icon = api.searchIcon(name)
                    if (icon != null) {
                        val f = api.downloadIcon(icon, cacheFile)
                        if (f != null) bmp = BitmapFactory.decodeFile(f.absolutePath)
                    }
                } else {
                    bmp = BitmapFactory.decodeFile(cacheFile.absolutePath)
                }
            } catch (e: Exception) { /* 忽略 */ }
            if (_binding != null) onDone(bmp)
        }
    }

    /** 抢到稀有商品：+1 许愿星，记录已抢到（每轮一次） */
    private fun onClaim(name: String) {
        val roundKey = MerchantPolicy.currentKey()
        val key = roundKey + "/" + name
        val claimed = prefs.getClaimed()
        if (claimed.contains(key)) {
            Toast.makeText(requireContext(), "本轮「" + name + "」已确认抢到", Toast.LENGTH_SHORT).show()
            return
        }
        claimed.add(key)
        prefs.setClaimed(claimed)
        val stars = prefs.stars + 1
        prefs.stars = stars
        val before = prefs.levelFromStars(stars - 1)
        val after = prefs.levelFromStars(stars)
        if (after > before) {
            Toast.makeText(requireContext(), "🎉 抢到「" + name + "」！许愿星+1，升级到 Lv." + after, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "抢到「" + name + "」！许愿星+1 ⭐", Toast.LENGTH_SHORT).show()
        }
        maybeUrgentNotify()
    }

    private fun updateCountdown() {
        val now = System.currentTimeMillis()
        val round = MerchantPolicy.currentRoundId(now)
        if (round == null) {
            binding.countdownText.text = "休市中 · 下一轮 08:00 开始"
            binding.countdownText.setTextColor(0xFF000000.toInt())
            return
        }
        val remain = MerchantPolicy.roundEndMs(round, now) - now
        val totalMin = remain / 60000
        val hh = totalMin / 60
        val mm = totalMin % 60
        val urgent = totalMin <= 15
        binding.countdownText.text = "⏳ 本轮剩余 " + hh + " 小时 " + mm + " 分钟" + if (urgent) " · 即将收摊！" else ""
        binding.countdownText.setTextColor(if (urgent) 0xFFC62828.toInt() else 0xFF2E7D32.toInt())
        if (urgent) maybeUrgentNotify()
    }

    private fun maybeUrgentNotify() {
        // 仅在距本轮收摊 ≤15 分钟时才提醒（防止 onResume/抢到时误触发）
        val now = System.currentTimeMillis()
        val round = MerchantPolicy.currentRoundId(now) ?: return
        val remain = MerchantPolicy.roundEndMs(round, now) - now
        if (remain > 15 * 60 * 1000L) return
        val roundKey = MerchantPolicy.currentKey()
        val notified = prefs.getNotified()
        if (notified.containsKey("urgent-" + roundKey)) return
        val wishes = prefs.getWishlist().filter { it.enabled }.map { it.name }.toSet()
        val claimed = prefs.getClaimed()
        val unclaimed = currentShelfNames.filter { it in wishes && !claimed.contains(roundKey + "/" + it) }
        if (unclaimed.isEmpty()) return
        Notifier(requireContext()).notifyArrival(
            "远行商人 · 即将收摊",
            "愿望商品「" + unclaimed.joinToString("、") + "」仍在售，剩余不足 15 分钟，快去抢购！",
            prefs.soundOn, prefs.vibrateOn, prefs.classMode
        )
        notified["urgent-" + roundKey] = System.currentTimeMillis()
        prefs.setNotified(notified)
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    override fun onResume() {
        super.onResume()
        if (::prefs.isInitialized) maybeAutoFetch()
        startTicker()
    }

    override fun onPause() {
        super.onPause()
        countdownTicker?.interrupt()
        countdownTicker = null
    }

    private fun startTicker() {
        countdownTicker?.interrupt()
        countdownTicker = Thread {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    Thread.sleep(30000)
                    if (_binding != null) requireActivity().runOnUiThread { updateCountdown() }
                } catch (e: InterruptedException) { break }
            }
        }.apply { isDaemon = true; start() }
    }

    override fun onDestroyView() {
        countdownTicker?.interrupt(); countdownTicker = null
        super.onDestroyView()
        _binding = null
    }
}
