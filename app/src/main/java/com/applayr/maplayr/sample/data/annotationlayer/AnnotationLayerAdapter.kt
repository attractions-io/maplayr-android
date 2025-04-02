package com.applayr.maplayr.sample.data.annotationlayer

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.ViewGroup
import android.widget.ImageView
import com.applayr.maplayr.androidLayer.annotation.CoordinateAnnotationLayer
import com.applayr.maplayr.androidLayer.annotation.CoordinateAnnotationViewHolder
import com.applayr.maplayr.androidLayer.annotation.defaultAnnotation.LabeledAnnotationIcon
import com.applayr.maplayr.sample.data.model.Attraction
import com.applayr.positionlayr.model.coordinate.GeographicCoordinate

class AnnotationLayerAdapter : CoordinateAnnotationLayer.Adapter<Attraction> {

    // Define a CoordinateAnnotationViewHolder containing a LabeledAnnotationIcon
    class LabeledAnnotationIconViewHolder(view: LabeledAnnotationIcon) : CoordinateAnnotationViewHolder(view)

    // Create an instance of the LabeledAnnotationIconViewHolder
    override fun createView(parent: ViewGroup, viewType: Int) = LabeledAnnotationIconViewHolder(LabeledAnnotationIcon(parent.context))

    // Bind the data for a given `Attraction` to the LabeledAnnotationIcon
    override fun bindView(coordinateAnnotationViewHolder: CoordinateAnnotationViewHolder, element: Attraction) {

        val labeledAnnotationIcon = coordinateAnnotationViewHolder.view as LabeledAnnotationIcon

        labeledAnnotationIcon.labelTextColor = ColorStateList.valueOf(Color.BLACK)

        labeledAnnotationIcon.labelText = element.name

        (labeledAnnotationIcon.annotationIcon as ImageView).setImageResource(element.iconImage)

    }

    // Provide the geographic coordinate for a given `Attraction`
    override fun annotationLocation(element: Attraction) = GeographicCoordinate(element.latitude, element.longitude)
}
