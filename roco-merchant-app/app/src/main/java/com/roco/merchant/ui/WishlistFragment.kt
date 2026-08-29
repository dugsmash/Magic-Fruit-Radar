package com.roco.merchant.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.roco.merchant.data.Prefs
import com.roco.merchant.data.WishItem
import com.roco.merchant.databinding.FragmentWishlistBinding
import com.roco.merchant.databinding.ItemWishBinding

class WishlistFragment : Fragment() {

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: Prefs
    private val adapter = WishAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        prefs = Prefs(requireContext())
        prefs.ensureDefaultWish() // 首次启动预置默认愿望
        binding.wishList.layoutManager = LinearLayoutManager(requireContext())
        binding.wishList.adapter = adapter
        binding.fabAdd.setOnClickListener {
            ItemPickerDialog(requireContext(), prefs) { refresh() }.show()
        }
        refresh()
    }

    override fun onResume() {
        super.onResume()
        if (::prefs.isInitialized) refresh()
    }

    fun refresh() {
        if (!::prefs.isInitialized) return
        val list = prefs.getWishlist()
        adapter.submit(list)
        binding.emptyText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        binding.merchantStatus.text = "🔍 商人状态：许愿 " + list.size + " 件 · 商人每轮刷新后 5 分钟自动检测"
        updateLevel()
    }

    /** 等级头：许愿星 */
    private fun updateLevel() {
        val level = prefs.levelFromStars()
        val inLevel = prefs.starsInLevel()
        val need = prefs.nextLevelNeed(level)
        binding.levelTitle.text = "⭐ Lv." + level
        binding.levelDetail.text = "许愿星 " + inLevel + "/" + need +
            " · 再抢 " + (need - inLevel).coerceAtLeast(0) + " 颗升级（每抢到 1 件稀有商品 +1 颗）"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class WishAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<WishAdapter.VH>() {
        private val items = mutableListOf<WishItem>()

        fun submit(list: List<WishItem>) {
            items.clear(); items.addAll(list)
            notifyDataSetChanged()
        }

        inner class VH(val b: ItemWishBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(ItemWishBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.b.wishName.text = item.name
            holder.b.wishName.startFlow() // 愿望单商品 = 彩色流动渐变（文字未变时动画不重置）
            holder.b.wishMeta.text = if (item.enabled) "盯梢中 · 到货提醒" else "已暂停 · 不提醒"
            // 先移除旧监听器再 setChecked，避免复用的 ViewHolder 触发上一个监听器（导致布局期 notify 闪退）
            holder.b.wishSwitch.setOnCheckedChangeListener(null)
            holder.b.wishSwitch.isChecked = item.enabled
            holder.b.wishSwitch.setOnCheckedChangeListener { _, checked ->
                prefs.setWishEnabled(item.name, checked)
                // 只原地更新该行的状态文字，不做全量刷新（否则彩虹动画会重置）
                holder.b.wishMeta.text = if (checked) "盯梢中 · 到货提醒" else "已暂停 · 不提醒"
            }
            // 移除按钮：从许愿单删除该商品
            holder.b.wishRemove.setOnClickListener {
                prefs.removeWish(item.name)
                Toast.makeText(requireContext(), "已从许愿单移除「" + item.name + "」", Toast.LENGTH_SHORT).show()
                refresh()
            }
        }
    }
}
