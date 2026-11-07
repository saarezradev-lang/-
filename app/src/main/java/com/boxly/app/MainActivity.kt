package com.boxly.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Image
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { BoxlyApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoxlyApp(vm: BoxViewModel = viewModel()) {
    var query by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }
    var showUpgrade by remember { mutableStateOf(false) }
    var qrBox by remember { mutableStateOf<Box?>(null) }
    val results = vm.search(query)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (vm.isPro) "BOXLY 📦 Pro" else "BOXLY 📦  ${vm.boxes.size}/$FREE_BOX_LIMIT") },
                actions = { if (!vm.isPro) TextButton(onClick = { showUpgrade = true }) { Text("Pro ₪20") } },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { if (vm.canAddBox) showAdd = true else showUpgrade = true }) {
                Icon(Icons.Default.Add, contentDescription = "ארגז חדש")
            }
        },
    ) { pad ->
        Column(Modifier.padding(pad).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                label = { Text("חיפוש ארגז, חדר או חפץ") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(results, key = { it.id }) { box ->
                    BoxCard(box, vm, onQr = { if (vm.isPro) qrBox = box else showUpgrade = true })
                }
            }
        }
    }

    qrBox?.let { QrDialog(it) { qrBox = null } }
    if (showAdd) AddBoxDialog(onDismiss = { showAdd = false }) { n, r, l -> vm.addBox(n, r, l); showAdd = false }
    if (showUpgrade) AlertDialog(
        onDismissRequest = { showUpgrade = false },
        title = { Text("Boxly Pro — ₪20") },
        text = { Text("ארגזים ללא הגבלה, מפה ומיקום GPS ו-QR לכל ארגז (בקרוב).\n\nגרסת בדיקה: השדרוג מקומי בלבד.") },
        confirmButton = { TextButton(onClick = { vm.upgradeToProForTesting(); showUpgrade = false }) { Text("שדרג (בדיקה)") } },
        dismissButton = { TextButton(onClick = { showUpgrade = false }) { Text("סגור") } },
    )
}

@Composable
fun BoxCard(box: Box, vm: BoxViewModel, onQr: () -> Unit) {
    var newItem by remember { mutableStateOf("") }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(box.name, style = MaterialTheme.typography.titleMedium)
                    Text(listOf(box.room, box.location).filter { it.isNotBlank() }.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onQr) { Icon(Icons.Default.QrCode, "QR לארגז") }
                IconButton(onClick = { vm.deleteBox(box.id) }) { Icon(Icons.Default.Delete, "מחק ארגז") }
            }
            box.items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("• $item", Modifier.weight(1f))
                    TextButton(onClick = { vm.removeItem(box.id, item) }) { Text("הסר") }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(newItem, { newItem = it }, Modifier.weight(1f), label = { Text("הוסף חפץ") }, singleLine = true)
                IconButton(onClick = { vm.addItem(box.id, newItem); newItem = "" }) { Icon(Icons.Default.Add, "הוסף") }
            }
        }
    }
}

@Composable
fun AddBoxDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var loc by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ארגז חדש") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("שם הארגז") }, singleLine = true)
                OutlinedTextField(room, { room = it }, label = { Text("חדר") }, singleLine = true)
                OutlinedTextField(loc, { loc = it }, label = { Text("מיקום (למשל מחסן)") }, singleLine = true)
            }
        },
        confirmButton = { TextButton(onClick = { onAdd(name, room, loc) }, enabled = name.isNotBlank()) { Text("הוסף") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ביטול") } },
    )
}

@Composable
fun QrDialog(box: Box, onDismiss: () -> Unit) {
    val bitmap = remember(box.id) { generateQr(boxQrPayload(box)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("QR — ${box.name}") },
        text = {
            Image(bitmap.asImageBitmap(), contentDescription = "QR של ${box.name}", modifier = Modifier.size(260.dp))
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("סגור") } },
    )
}
