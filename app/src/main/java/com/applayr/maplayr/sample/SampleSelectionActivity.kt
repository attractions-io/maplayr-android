package com.applayr.maplayr.sample

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class SampleSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_selection)

        findViewById<View>(R.id.extended_sample).setOnClickListener {
            startActivity(Intent(this, ExtendedSampleActivity::class.java))
        }

        findViewById<View>(R.id.not_bundled_sample).setOnClickListener {
            startActivity(Intent(this, NotBundledSampleActivity::class.java))
        }

        findViewById<View>(R.id.non_map_view_sample).setOnClickListener {
            startActivity(Intent(this, NonMapViewSampleActivity::class.java))
        }
    }
}
