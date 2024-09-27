package com.applayr.maplayr.sample.data.model

import java.lang.ref.WeakReference

class QueueTimeHandler {

    private var queueTime: Int? = null

    private var queueTimeTextViewWeakReference: WeakReference<QueueTimeTextView>? = null

    fun setQueueTime(queueTime: Int?) {
        this.queueTime = queueTime
        apply()
    }

    fun attachTextView(textView: QueueTimeTextView) {
        textView.setQueueTimeHandler(this)
        queueTimeTextViewWeakReference = WeakReference(textView)
        apply()
    }

    private fun apply() {
        queueTimeTextViewWeakReference?.get()?.text = queueTime?.let { queueTime -> "$queueTime min" }
    }

    fun detachTextView() {
        queueTimeTextViewWeakReference?.clear()
        queueTimeTextViewWeakReference = null
    }
}