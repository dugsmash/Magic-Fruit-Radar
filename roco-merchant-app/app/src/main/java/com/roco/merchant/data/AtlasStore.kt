package com.roco.merchant.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

/** 道具图鉴：内置 assets/atlas.json（随包）为初始数据，files/atlas.json 为手动补全后的最新数据 */
object AtlasStore {

    data class AtlasItem(val name: String, val icon: String)

    private const val ASSET_FILE = "atlas.json"

    private val gson = Gson()

    fun atlasFile(context: Context): File = File(context.filesDir, "atlas.json")

    /** 读取图鉴：优先本地补全后的文件，否则回退到内置 assets/atlas.json */
    fun load(context: Context): List<AtlasItem> {
        val f = atlasFile(context)
        if (f.exists()) {
            val local = parse(f.readText())
            if (local.isNotEmpty()) return local
        }
        return try {
            context.assets.open(ASSET_FILE).use { parse(it.bufferedReader().readText()) }
        } catch (e: Exception) { emptyList() }
    }

    private fun parse(text: String): List<AtlasItem> = try {
        val list: List<AtlasItem>? = gson.fromJson(text, object : TypeToken<List<AtlasItem>>() {}.type)
        list ?: emptyList()
    } catch (e: Exception) { emptyList() }

    fun save(context: Context, items: List<AtlasItem>) {
        atlasFile(context).writeText(gson.toJson(items))
    }

    /** 合并去重（按名称，保留首个），过滤空名 */
    fun merge(existing: List<AtlasItem>, incoming: List<AtlasItem>): List<AtlasItem> {
        val seen = HashSet<String>()
        val out = ArrayList<AtlasItem>()
        for (i in existing + incoming) {
            val n = i.name.trim()
            if (n.isEmpty()) continue
            if (seen.add(n)) out.add(i.copy(name = n))
        }
        return out
    }

    /** 本地按名称搜索 */
    fun search(items: List<AtlasItem>, q: String): List<AtlasItem> {
        if (q.isBlank()) return items
        return items.filter { it.name.contains(q.trim()) }
    }
}
