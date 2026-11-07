package com.boxly.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** שמירה קבועה פשוטה של הארגזים ב-SharedPreferences (JSON). */
class BoxStore(context: Context) {
    private val prefs = context.getSharedPreferences("boxly", Context.MODE_PRIVATE)

    fun load(): List<Box> = try {
        val arr = JSONArray(prefs.getString(KEY, "[]"))
        List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            val items = o.getJSONArray("items")
            Box(
                id = o.getLong("id"),
                name = o.getString("name"),
                room = o.getString("room"),
                location = o.getString("location"),
                items = List(items.length()) { items.getString(it) },
            )
        }
    } catch (e: Exception) {
        emptyList()
    }

    fun save(boxes: List<Box>) {
        val arr = JSONArray()
        boxes.forEach { b ->
            arr.put(JSONObject().apply {
                put("id", b.id)
                put("name", b.name)
                put("room", b.room)
                put("location", b.location)
                put("items", JSONArray(b.items))
            })
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    private companion object { const val KEY = "boxes" }
}
