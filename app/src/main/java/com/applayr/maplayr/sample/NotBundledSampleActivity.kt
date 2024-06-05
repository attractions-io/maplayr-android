package com.applayr.maplayr.sample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.applayr.maplayr.MapView
import com.applayr.maplayr.model.map.DownloadResult
import com.applayr.maplayr.model.map.Map

class NotBundledSampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_not_bundled_sample)

        val mapView = findViewById<MapView>(R.id.map_view)

        mapView.setMap(Map.managed(context = applicationContext, idString = "df98bfa3-156c-49cb-9f94-1b9ec52a08c4"))

        mapView.isVisible = false

        mapView.mapContextLiveData.observe(lifecycleOwner = mapView) { mapContext ->
            Log.d("NotBundledSample", "Map context changed: $mapContext")
            mapView.isVisible = mapContext != null
        }

        mapView.downloadResultLiveData.observe(lifecycleOwner = mapView) { downloadResult ->
            when (downloadResult) {
                is DownloadResult.Success -> Log.d("NotBundledSample", "Download result is success")
                DownloadResult.NoUpdateAvailable -> Log.d("NotBundledSample", "Download result is no update available")
                is DownloadResult.Failure -> Log.d("NotBundledSample", "Download result is failure")
                DownloadResult.Unauthorized -> Log.d("NotBundledSample", "Download result is unauthorized")
                null -> Log.d("NotBundledSample", "Download result is null")
            }
        }
    }
}
