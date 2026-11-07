package com.boxly.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class Box(
    val id: Long,
    val name: String,
    val room: String,
    val location: String,
    val items: List<String> = emptyList(),
)

const val FREE_BOX_LIMIT = 20

class BoxViewModel : ViewModel() {
    val boxes = mutableStateListOf<Box>()
    var isPro by mutableStateOf(false)
        private set
    private var nextId = 1L

    val canAddBox: Boolean get() = isPro || boxes.size < FREE_BOX_LIMIT

    fun addBox(name: String, room: String, location: String): Boolean {
        if (!canAddBox || name.isBlank()) return false
        boxes.add(Box(nextId++, name.trim(), room.trim(), location.trim()))
        return true
    }

    fun deleteBox(id: Long) { boxes.removeAll { it.id == id } }

    fun addItem(boxId: Long, item: String) {
        if (item.isBlank()) return
        val i = boxes.indexOfFirst { it.id == boxId }
        if (i >= 0) boxes[i] = boxes[i].copy(items = boxes[i].items + item.trim())
    }

    fun removeItem(boxId: Long, item: String) {
        val i = boxes.indexOfFirst { it.id == boxId }
        if (i >= 0) boxes[i] = boxes[i].copy(items = boxes[i].items - item)
    }

    fun search(query: String): List<Box> {
        val q = query.trim()
        if (q.isEmpty()) return boxes
        return boxes.filter { b ->
            b.name.contains(q, true) || b.room.contains(q, true) ||
                b.location.contains(q, true) || b.items.any { it.contains(q, true) }
        }
    }

    // TODO לפני פרסום: להחליף ב-Google Play Billing מלא עם אימות רכישה.
    fun upgradeToProForTesting() { isPro = true }
}
