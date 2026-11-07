package com.boxly.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

data class Box(
    val id: Long,
    val name: String,
    val room: String,
    val location: String,
    val items: List<String> = emptyList(),
    val lat: Double? = null,
    val lng: Double? = null,
)

const val FREE_BOX_LIMIT = 20

class BoxViewModel(app: Application) : AndroidViewModel(app) {
    private val store = BoxStore(app)
    val boxes = mutableStateListOf<Box>().apply { addAll(store.load()) }
    var isPro by mutableStateOf(false)
        private set
    val billing = BillingManager(app, viewModelScope) { isPro = it }

    init { billing.connect() }
    private var nextId = (boxes.maxOfOrNull { it.id } ?: 0L) + 1

    val canAddBox: Boolean get() = isPro || boxes.size < FREE_BOX_LIMIT

    fun addBox(name: String, room: String, location: String): Boolean {
        if (!canAddBox || name.isBlank()) return false
        boxes.add(Box(nextId++, name.trim(), room.trim(), location.trim()))
        persist()
        return true
    }

    fun deleteBox(id: Long) { boxes.removeAll { it.id == id }; persist() }

    fun addItem(boxId: Long, item: String) {
        if (item.isBlank()) return
        val i = boxes.indexOfFirst { it.id == boxId }
        if (i >= 0) {
            boxes[i] = boxes[i].copy(items = boxes[i].items + item.trim())
            persist()
        }
    }

    fun removeItem(boxId: Long, item: String) {
        val i = boxes.indexOfFirst { it.id == boxId }
        if (i >= 0) {
            boxes[i] = boxes[i].copy(items = boxes[i].items - item)
            persist()
        }
    }

    fun search(query: String): List<Box> {
        val q = query.trim()
        if (q.isEmpty()) return boxes
        return boxes.filter { b ->
            b.name.contains(q, true) || b.room.contains(q, true) ||
                b.location.contains(q, true) || b.items.any { it.contains(q, true) }
        }
    }

    fun setLocation(boxId: Long, lat: Double, lng: Double) {
        val i = boxes.indexOfFirst { it.id == boxId }
        if (i >= 0) {
            boxes[i] = boxes[i].copy(lat = lat, lng = lng)
            persist()
        }
    }

    private fun persist() = store.save(boxes.toList())

    override fun onCleared() { billing.disconnect() }
}
