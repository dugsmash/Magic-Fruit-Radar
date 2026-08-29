package com.roco.merchant.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.roco.merchant.data.AtlasRepository
import com.roco.merchant.data.AtlasStore
import com.roco.merchant.data.Prefs
import com.roco.merchant.data.WikiItemsApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 道具图鉴选择弹窗：包含全部图鉴，支持搜索/下滑浏览，点击加入（或移除）许愿单 */
class ItemPickerDialog(
    private val context: android.content.Context,
    private val prefs: Prefs,
    private val onChanged: () -> Unit
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var all: List<AtlasStore.AtlasItem> = emptyList()
    private var shown: List<AtlasStore.AtlasItem> = emptyList()
    private val inFlightIcons = HashSet<String>()
    private val adapter = PickerAdapter()

    private lateinit var dialog: AlertDialog
    private lateinit var listView: ListView
    private lateinit var statusText: TextView

    fun show() {
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(8), dp(14), dp(8))
        }
        val search = EditText(context).apply {
            hint = "🔍 搜索道具名称（如：棱镜 / 血脉 / 蛋）"
            setHintTextColor(0xFF000000.toInt())
            textSize = 14f
            setSingleLine(true)
            setPadding(dp(10), dp(6), dp(10), dp(6))
            setBackgroundResource(android.R.drawable.edit_text)
        }
        statusText = TextView(context).apply {
            textSize = 12f
            setTextColor(0xFF000000.toInt())
            setPadding(dp(2), dp(8), dp(2), dp(4))
        }
        listView = ListView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
            adapter = this@ItemPickerDialog.adapter
        }
        root.addView(search)
        root.addView(statusText)
        root.addView(listView)

        dialog = AlertDialog.Builder(context)
            .setTitle("📖 道具图鉴 · 点击加入许愿单")
            .setView(root)
            .setNegativeButton("关闭", null)
            .create()
        dialog.setOnDismissListener { scope.cancel() }
        dialog.show()

        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                shown = AtlasStore.search(all, s?.toString() ?: "")
                adapter.notifyDataSetChanged()
                updateStatus()
            }
        })

        listView.setOnItemClickListener { _, _, pos, _ ->
            val item = shown.getOrNull(pos) ?: return@setOnItemClickListener
            val inWish = prefs.getWishlist().any { it.name == item.name }
            if (inWish) {
                prefs.removeWish(item.name)
                Toast.makeText(context, "已从许愿单移除「" + item.name + "」", Toast.LENGTH_SHORT).show()
            } else {
                prefs.addWish(item.name)
                Toast.makeText(context, "已加入许愿单「" + item.name + "」", Toast.LENGTH_SHORT).show()
            }
            onChanged()
            adapter.notifyDataSetChanged()
            updateStatus()
        }

        scope.launch {
            statusText.text = "正在加载图鉴…"
            val loaded = withContext(Dispatchers.IO) { AtlasStore.load(context) }
            all = loaded
            shown = loaded
            adapter.notifyDataSetChanged()
            updateStatus()
            if (loaded.isEmpty()) {
                statusText.text = "图鉴为空，正在从洛克魔法书拉取全部道具与图片…"
                syncAtlas()
            }
        }
    }

    /** 后台全量同步图鉴（仅手动触发） */
    private fun syncAtlas() {
        scope.launch {
            val p = AtlasRepository.sync(context) { prog ->
                scope.launch {
                    statusText.text = "图鉴同步中… 已收录 " + prog.totalItems + " 件 · 图标补全 " + prog.downloaded + " 张"
                }
            }
            val loaded = withContext(Dispatchers.IO) { AtlasStore.load(context) }
            all = loaded
            shown = loaded
            adapter.notifyDataSetChanged()
            updateStatus()
            if (p.error != null) {
                statusText.text = "⚠️ " + p.error
                Toast.makeText(context, p.error, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "图鉴同步完成：共 " + loaded.size + " 件道具", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateStatus() {
        val wished = prefs.getWishlist().size
        statusText.text = "共 " + all.size + " 件 · 显示 " + shown.size + " 件 · 已加入许愿单 " + wished + " 件"
    }

    private fun loadIcon(holder: Holder, item: AtlasStore.AtlasItem) {
        val marker = item.name
        scope.launch {
            // 优先内置 assets/items/，其次本地缓存；都没有再按需下载
            val bmp = withContext(Dispatchers.IO) { WikiItemsApi.loadIconBitmap(context, item.name) }
            if (bmp != null) {
                if (holder.name.tag == marker) holder.icon.setImageBitmap(bmp)
                return@launch
            }
            if (inFlightIcons.add(marker)) {
                val cache = WikiItemsApi.cacheFile(context, item.name)
                val ok = withContext(Dispatchers.IO) {
                    try {
                        val api = WikiItemsApi(prefs.baseUrl, prefs.apiKey)
                        api.downloadIcon(item.icon, cache) != null
                    } catch (e: Exception) { false }
                }
                val dl = if (ok) withContext(Dispatchers.IO) { WikiItemsApi.loadIconBitmap(context, item.name) } else null
                if (holder.name.tag == marker && dl != null) holder.icon.setImageBitmap(dl)
            }
        }
    }

    private inner class PickerAdapter : BaseAdapter() {
        override fun getCount() = shown.size
        override fun getItem(pos: Int) = shown[pos]
        override fun getItemId(pos: Int) = pos.toLong()

        override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
            val item = shown[pos]
            val holder: Holder
            val row: View
            if (convertView == null) {
                row = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(4), dp(6), dp(4), dp(6))
                }
                val icon = ImageView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(dp(48), dp(48))
                }
                val mid = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setPadding(dp(10), 0, dp(6), 0)
                }
                val name = TextView(context).apply {
                    textSize = 14f; maxLines = 1
                    ellipsize = android.text.TextUtils.TruncateAt.END
                }
                val state = TextView(context).apply { textSize = 12f }
                mid.addView(name)
                mid.addView(state)
                row.addView(icon)
                row.addView(mid)
                holder = Holder(icon, name, state)
                row.tag = holder
            } else {
                holder = convertView.tag as Holder
                row = convertView
            }
            holder.name.text = item.name
            holder.name.tag = item.name // 绑定标记，防止异步图片错位
            holder.icon.setImageDrawable(null)
            val inWish = prefs.getWishlist().any { it.name == item.name }
            holder.state.text = if (inWish) "✅ 已加入（点击移除）" else "＋ 加入许愿单"
            holder.state.setTextColor(if (inWish) 0xFF2E7D32.toInt() else 0xFF000000.toInt())
            loadIcon(holder, item)
            return row
        }
    }

    private class Holder(val icon: ImageView, val name: TextView, val state: TextView)

    private fun dp(v: Int): Int = (v * context.resources.displayMetrics.density).toInt()
}
