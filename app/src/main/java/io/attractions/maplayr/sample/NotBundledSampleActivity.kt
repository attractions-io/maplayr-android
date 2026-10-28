package io.attractions.maplayr.sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import io.attractions.livedata.LifecycleOwner
import io.attractions.maplayr.MapView
import io.attractions.maplayr.model.map.DownloadResult
import io.attractions.maplayr.model.map.Map
import io.attractions.maplayr.model.map.MapVersionResult

class NotBundledSampleActivity : AppCompatActivity() {

    private val mapLifecycleOwner = LifecycleOwner()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_not_bundled_sample)

        val mapView = findViewById<MapView>(R.id.map_view)

        val map = Map.managed(mapId = "df98bfa3-156c-49cb-9f94-1b9ec52a08c4")

        mapView.setMap(map)

        mapView.isVisible = false

        map.mapVersionResult.observe(lifecycleOwner = mapLifecycleOwner) { mapVersionResult ->
            Log.d("NotBundledSample", "Map version result changed: $mapVersionResult")
            mapView.isVisible = mapVersionResult is MapVersionResult.Available
        }

        map.downloadResult.observe(lifecycleOwner = mapLifecycleOwner) { downloadResult ->
            when (downloadResult) {
                is DownloadResult.Success -> Log.d("NotBundledSample", "Download result is success")
                DownloadResult.NoUpdateAvailable -> Log.d("NotBundledSample", "Download result is no update available")
                is DownloadResult.Failure -> Log.d("NotBundledSample", "Download result is failure")
                DownloadResult.Unauthorized -> Log.d("NotBundledSample", "Download result is unauthorized")
                null -> Log.d("NotBundledSample", "Download result is null")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapLifecycleOwner.cancelAll()
    }
}
