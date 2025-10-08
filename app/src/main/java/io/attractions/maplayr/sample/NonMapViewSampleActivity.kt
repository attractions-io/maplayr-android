package io.attractions.maplayr.sample

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.attractions.livedata.LifecycleOwner
import io.attractions.livedata.LifecycleOwnerImpl
import io.attractions.livedata.zipWith
import io.attractions.maplayr.model.map.DownloadResult
import io.attractions.maplayr.model.map.Map
import io.attractions.positioning.google.GoogleLocationId
import io.attractions.positioning.model.coordinate.Location
import io.attractions.positioning.model.coordinate.LocationResponse
import io.attractions.positioning.model.opengl.location.GlobalLocationManager

class NonMapViewSampleActivity : AppCompatActivity() {

    private var lifecycleOwner: LifecycleOwner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_non_map_view_sample)
        val mapStatusTextView = findViewById<TextView>(R.id.map_status_text_view)
        val locationStatusTextView = findViewById<TextView>(R.id.location_status_text_view)

        val lifecycleOwner: LifecycleOwner = LifecycleOwnerImpl()
        this.lifecycleOwner = lifecycleOwner

        val map = Map.managed(
            mapId = "df98bfa3-156c-49cb-9f94-1b9ec52a08c4"
        )

        (map.downloadResultLiveData zipWith map.mapContextLiveData).observe(lifecycleOwner = lifecycleOwner) { (downloadResult, mapContext) ->
            mapStatusTextView.text = if (mapContext != null) {
                if (downloadResult != null) {
                    "App already has version ${mapContext.version} locally, remote download result is ${downloadResult.prettyName()}"
                } else {
                    "App already has version ${mapContext.version} locally, checking for update..."
                }
            } else {
                if (downloadResult != null) {
                    "Download has failed to get map due to ${downloadResult.prettyName()} and the app has no map locally"
                } else {
                    "Downloading map..."
                }
            }
        }

        GlobalLocationManager.getLocationLiveData(GoogleLocationId.HighAccuracy).observe(lifecycleOwner = lifecycleOwner) { locationResponse ->
            when (locationResponse) {
                is Location -> locationStatusTextView.text = "Location: ${locationResponse.geographicCoordinate}"
                LocationResponse.NotPermitted -> locationStatusTextView.text = "Location permissions not granted."
                LocationResponse.Pending -> locationStatusTextView.text = "Location pending..."
            }
        }
    }

    override fun onDestroy() {
        lifecycleOwner = null
        super.onDestroy()
    }

    private companion object {

        private fun DownloadResult.prettyName(): String {
            return when (this) {
                is DownloadResult.Success -> "Success"
                is DownloadResult.Failure -> "Failure"
                DownloadResult.NoUpdateAvailable -> "No update available"
                DownloadResult.Unauthorized -> "Unauthorized"
            }
        }
    }
}
