package com.rogers.eventapp.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.math.*

object LocationUtils {

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    suspend fun getCurrentLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) return null
        if (!isLocationEnabled(context)) return null

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()

        return suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { location ->
                    continuation.resume(location)
                }.addOnFailureListener {
                    continuation.resume(null)
                }
            } catch (e: SecurityException) {
                continuation.resume(null)
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }
}

object DistanceUtils {

    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Haversine formula to calculate distance between two lat/lng points.
     * Returns distance in kilometres.
     */
    fun calculateDistanceKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    /**
     * Returns a human-readable distance string.
     * e.g. "1.2 km", "850 m", "12 km"
     */
    fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 0.1 -> "Nearby"
            distanceKm < 1.0 -> "${(distanceKm * 1000).toInt()} m"
            distanceKm < 10.0 -> String.format("%.1f km", distanceKm)
            else -> "${distanceKm.toInt()} km"
        }
    }
}

object DeepLinkUtils {

    /**
     * Opens Google Maps (or any maps app) for navigation to the given location.
     * Falls back to browser Maps if no maps app is installed.
     */
    fun openMapsForNavigation(
        context: Context,
        latitude: Double,
        longitude: Double,
        label: String
    ) {
        val encodedLabel = Uri.encode(label)
        val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)")
        val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        if (mapsIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapsIntent)
        } else {
            // Fallback: open in browser
            val browserUri = Uri.parse(
                "https://maps.google.com/?q=$latitude,$longitude"
            )
            context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    }

    /**
     * Builds a deep link URI for an event detail screen.
     * Format: eventsapp://event/{eventId}
     */
    fun buildEventDeepLink(eventId: String): Uri {
        return Uri.parse("eventsapp://event/$eventId")
    }

    /**
     * Shares an event via system share sheet.
     */
    fun shareEvent(context: Context, eventTitle: String, eventId: String) {
        val shareText = "Check out this event: $eventTitle\n" +
                "eventsapp://event/$eventId"
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Event"))
    }
}

object DateUtils {

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val displayDateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    private val displayTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val displayFullFormat = SimpleDateFormat("EEE, dd MMM · hh:mm a", Locale.getDefault())

    fun parseIso(isoString: String): Date? = try {
        isoFormat.parse(isoString)
    } catch (e: Exception) {
        null
    }

    fun formatDisplayDate(isoString: String): String {
        val date = parseIso(isoString) ?: return isoString
        return displayDateFormat.format(date)
    }

    fun formatDisplayTime(isoString: String): String {
        val date = parseIso(isoString) ?: return isoString
        return displayTimeFormat.format(date)
    }

    fun formatDisplayFull(isoString: String): String {
        val date = parseIso(isoString) ?: return isoString
        return displayFullFormat.format(date)
    }

    fun isUpcoming(isoString: String): Boolean {
        val date = parseIso(isoString) ?: return false
        return date.after(Date())
    }

    fun daysUntilEvent(isoString: String): Int {
        val eventDate = parseIso(isoString) ?: return -1
        val now = Date()
        val diffMs = eventDate.time - now.time
        return (diffMs / (1000 * 60 * 60 * 24)).toInt()
    }

    fun getRelativeTime(isoString: String): String {
        val days = daysUntilEvent(isoString)
        return when {
            days < 0 -> "Ended"
            days == 0 -> "Today"
            days == 1 -> "Tomorrow"
            days < 7 -> "In $days days"
            days < 30 -> "In ${days / 7} weeks"
            else -> formatDisplayDate(isoString)
        }
    }
}
