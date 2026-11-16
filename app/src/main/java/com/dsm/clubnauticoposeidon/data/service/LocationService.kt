package com.dsm.clubnauticoposeidon.data.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationService(private val context: Context) {

    @SuppressLint("MissingPermission")
    suspend fun obtenerCoordenadasActuales(): Pair<Double, Double> = withContext(Dispatchers.IO) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            val lat = location?.latitude ?: -12.046374
            val lng = location?.longitude ?: -77.142786
            Pair(lat, lng)
        } catch (e: Exception) {
            Pair(-12.046374, -77.142786)
        }
    }
}
