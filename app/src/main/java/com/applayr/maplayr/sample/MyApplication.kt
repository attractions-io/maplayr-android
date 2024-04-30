package com.applayr.maplayr.sample

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.applayr.maplayr.model.map.Map

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // Process lifecycle owner
        ProcessLifecycleOwner.get().lifecycle.addObserver(object: DefaultLifecycleObserver {

            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                // Application moved to foreground
                Map.checkForUpdates(this@MyApplication)
            }

        })
    }

}