package com.boxly.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri

/** המיקום האחרון הידוע מהמכשיר, או null. דורש הרשאת מיקום. */
@SuppressLint("MissingPermission")
fun lastKnownLocation(context: Context): Location? = try {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    lm.getProviders(true).mapNotNull { lm.getLastKnownLocation(it) }.maxByOrNull { it.time }
} catch (e: SecurityException) {
    null
}

/** פותח את המיקום באפליקציית מפות כלשהי במכשיר (בלי מפתח API). */
fun openOnMap(context: Context, box: Box) {
    val lat = box.lat ?: return
    val lng = box.lng ?: return
    val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(box.name)})")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "לא נמצאה אפליקציית מפות", android.widget.Toast.LENGTH_SHORT).show()
    }
}
