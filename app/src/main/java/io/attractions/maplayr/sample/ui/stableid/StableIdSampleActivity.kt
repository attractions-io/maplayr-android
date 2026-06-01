package io.attractions.maplayr.sample.ui.stableid

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.attractions.maplayr.MapView
import io.attractions.maplayr.androidLayer.annotation.CoordinateAnnotationLayer
import io.attractions.maplayr.model.map.Map
import io.attractions.maplayr.sample.BuildConfig
import io.attractions.maplayr.sample.R
import io.attractions.maplayr.sample.data.annotationlayer.StableIdAdapter
import kotlinx.coroutines.launch

class StableIdSampleActivity : AppCompatActivity() {

    private val viewModel: StableIdViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stable_id_sample)

        val mapView = findViewById<MapView>(R.id.map_view)

        mapView.setMap(Map.Companion.managed(BuildConfig.MAPLAYR_MAP))

        val adapter = StableIdAdapter()
        val annotationLayer = CoordinateAnnotationLayer(this, adapter)
        mapView.addMapLayer(annotationLayer)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.attractions.collect { attractions ->
                    annotationLayer.replace(attractions)
                }
            }
        }
    }
}
