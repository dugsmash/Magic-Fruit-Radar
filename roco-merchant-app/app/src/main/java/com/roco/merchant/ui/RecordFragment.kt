package com.roco.merchant.ui

import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.roco.merchant.data.GoodsCatalog
import com.roco.merchant.data.MerchantRecord
import com.roco.merchant.data.Prefs
import com.roco.merchant.databinding.FragmentRecordBinding
import com.roco.merchant.databinding.ItemRecordBinding

/** 「记录」页：售卖记录 + 商品搜索 + 稀有商品最近出现时间 */
class RecordFragment : Fragment() {

    private var _binding: FragmentRecordBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: Prefs
    private val adapter = RecordAdapter()
    private var allRecords: List<MerchantRecord> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        prefs = Prefs(requireContext())
        binding.recordList.layoutManager = LinearLayoutManager(requireContext())
        binding.recordList.adapter = adapter
        binding.recordSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val kw = s?.toString()?.trim() ?: ""
                val filtered = if (kw.isEmpty()) allRecords
                    else allRecords.filter { r -> r.items.any { it.contains(kw) } }
                adapter.submit(filtered)
                binding.recordEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
            }
        })
        refresh()
    }

    override fun onResume() {
        super.onResume()
        if (::prefs.isInitialized) refresh()
    }

    fun refresh() {
        if (!::prefs.isInitialized) return
        allRecords = prefs.getRecords().asReversed()
        adapter.submit(allRecords)
        binding.recordEmpty.visibility = if (allRecords.isEmpty()) View.VISIBLE else View.GONE
        updateRareInfo()
    }

    /** 顶部：稀有商品最近出现时间 */
    private fun updateRareInfo() {
        val recs = prefs.getRecords()
        var latest: MerchantRecord? = null
        var latestRare = ""
        for (r in recs) {
            for (item in r.items) {
                if (GoodsCatalog.isRare(item)) {
                    if (latest == null || r.fetchedAtMs > latest.fetchedAtMs) {
                        latest = r; latestRare = item
                    }
                }
            }
        }
        binding.rareInfo.text = if (latest == null)
            "🔴 稀有商品最近出现：还未出现"
        else
            "🔴 稀有商品最近出现：" + latestRare + " · " + formatDate(latest.dateKey) + " " + latest.roundLabel
    }

    private fun formatDate(key: String): String {
        if (key.length != 8) return key
        return key.substring(0, 4) + "-" + key.substring(4, 6) + "-" + key.substring(6, 8)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class RecordAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<RecordAdapter.VH>() {
        private val items = mutableListOf<MerchantRecord>()

        fun submit(list: List<MerchantRecord>) {
            items.clear(); items.addAll(list)
            notifyDataSetChanged()
        }

        inner class VH(val b: ItemRecordBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val r = items[position]
            holder.b.recDate.text = formatDate(r.dateKey)
            holder.b.recCount.text = r.items.size.toString() + " 件"
            holder.b.recRound.text = "时段：" + r.roundLabel
            holder.b.recItems.text = r.items.joinToString("　")
        }
    }
}
