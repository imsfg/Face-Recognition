package com.faceattend.app.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

data class Coordinates(val latitude: Double, val longitude: Double)

class LocationHelper(private val context: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** Null when permission is missing or no fix is available — attendance still proceeds. */
    @SuppressLint("MissingPermission")
    suspend fun currentLocation(): Coordinates? {
        if (!hasPermission()) return null
        return suspendCancellableCoroutine { continuation ->
            val cancellation = CancellationTokenSource()
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellation.token)
                .addOnSuccessListener { location ->
                    continuation.resume(location?.let { Coordinates(it.latitude, it.longitude) })
                }
                .addOnFailureListener { continuation.resume(null) }
            continuation.invokeOnCancellation { cancellation.cancel() }
        }
    }
}
