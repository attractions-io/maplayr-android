package com.applayr.maplayr.sample.data.model

import android.content.Context
import android.widget.TextView
import java.lang.ref.WeakReference

class QueueTimeTextView(context: Context) : TextView(context) {

    private var queueTimeHandlerWeakReference: WeakReference<QueueTimeHandler>? = null

    fun setQueueTimeHandler(queueTimeHandler: QueueTimeHandler) {
        queueTimeHandlerWeakReference?.get()?.detachTextView()
        queueTimeHandlerWeakReference = WeakReference(queueTimeHandler)
    }
}