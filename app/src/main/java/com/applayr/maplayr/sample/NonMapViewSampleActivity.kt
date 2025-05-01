package com.applayr.maplayr.sample

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.applayr.livedata.LifecycleOwner
import com.applayr.livedata.LifecycleOwnerImpl
import com.applayr.livedata.zipWith
import com.applayr.maplayr.model.map.DownloadResult
import com.applayr.maplayr.model.map.Map

class NonMapViewSampleActivity : AppCompatActivity() {

    private var lifecycleOwner: LifecycleOwner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_non_map_view_sample)
        val textView = findViewById<TextView>(R.id.text_view)

        val lifecycleOwner: LifecycleOwner = LifecycleOwnerImpl()
        this.lifecycleOwner = lifecycleOwner

        val map = Map.managed(
            idString = "df98bfa3-156c-49cb-9f94-1b9ec52a08c4"
        )

        (map.downloadResultLiveData zipWith map.mapContextLiveData).observe(lifecycleOwner = lifecycleOwner) { (downloadResult, mapContext) ->
            textView.text = if (mapContext != null) {
                if (downloadResult != null) {
                    "App already has version ${mapContext.version} locally, remote download result is ${downloadResult.prettyName()}"
                } else {
                    "App already has version ${mapContext.version} locally, checking for update..."
                }
            } else {
                if (downloadResult != null) {
                    "Download has failed to get map due to ${downloadResult.prettyName()} and the app has no map locally"
                } else {
                    "Downloadind map..."
                }
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
