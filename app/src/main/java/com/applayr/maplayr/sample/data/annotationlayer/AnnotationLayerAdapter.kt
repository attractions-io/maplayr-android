package com.applayr.maplayr.sample.data.annotationlayer

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.applayr.maplayr.androidLayer.annotation.CoordinateAnnotationLayer
import com.applayr.maplayr.androidLayer.annotation.CoordinateAnnotationViewHolder
import com.applayr.maplayr.androidLayer.annotation.defaultAnnotation.LabeledAnnotationIcon
import com.applayr.maplayr.model.coordinate.GeographicCoordinate
import com.applayr.maplayr.sample.data.model.Attraction
import com.applayr.maplayr.sample.data.model.QueueTimeTextView

class AnnotationLayerAdapter : CoordinateAnnotationLayer.Adapter<Attraction> {

    // Define a CoordinateAnnotationViewHolder containing a LabeledAnnotationIcon
    class LabeledAnnotationIconViewHolder(view: LabeledAnnotationIconWithQueueTime) : CoordinateAnnotationViewHolder(view)

    // Create an instance of the LabeledAnnotationIconViewHolder
    override fun createView(parent: ViewGroup, viewType: Int) = LabeledAnnotationIconViewHolder(LabeledAnnotationIconWithQueueTime(parent.context))

    class LabeledAnnotationIconWithQueueTime(context: android.content.Context) : FrameLayout(context) {

        val labeledAnnotationIcon = LabeledAnnotationIcon(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            this@LabeledAnnotationIconWithQueueTime.addView(this)
        }

        val queueTimeTextView = QueueTimeTextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            setBackgroundColor(Color.BLACK)
            this@LabeledAnnotationIconWithQueueTime.addView(this)
        }

        init {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }
    }

    // Bind the data for a given `Attraction` to the LabeledAnnotationIcon
    override fun bindView(coordinateAnnotationViewHolder: CoordinateAnnotationViewHolder, element: Attraction) {

        val labeledAnnotationIcon = coordinateAnnotationViewHolder.view as LabeledAnnotationIconWithQueueTime

        labeledAnnotationIcon.labeledAnnotationIcon.labelTextColor = ColorStateList.valueOf(Color.BLACK)

        labeledAnnotationIcon.labeledAnnotationIcon.labelText = element.name

        (labeledAnnotationIcon.labeledAnnotationIcon.annotationIcon as ImageView).setImageResource(element.iconImage)

        element.queueTimeHandler.attachTextView(labeledAnnotationIcon.queueTimeTextView)
    }

    // Provide the geographic coordinate for a given `Attraction`
    override fun annotationLocation(element: Attraction) = GeographicCoordinate(element.latitude, element.longitude)
}
