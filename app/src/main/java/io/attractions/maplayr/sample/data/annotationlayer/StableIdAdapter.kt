package io.attractions.maplayr.sample.data.annotationlayer

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.ViewGroup
import android.widget.ImageView
import io.attractions.maplayr.androidLayer.annotation.CoordinateAnnotationLayer
import io.attractions.maplayr.androidLayer.annotation.CoordinateAnnotationViewHolder
import io.attractions.maplayr.androidLayer.annotation.defaultAnnotation.LabeledAnnotationIcon
import io.attractions.maplayr.sample.data.model.StableIdAttraction
import io.attractions.positioning.model.coordinate.GeographicCoordinate

class StableIdAdapter: CoordinateAnnotationLayer.Adapter<StableIdAttraction> {

    private class ViewHolder(view: LabeledAnnotationIcon) : CoordinateAnnotationViewHolder(view)

    override fun createView(parent: ViewGroup, viewType: Int): CoordinateAnnotationViewHolder =
        ViewHolder(LabeledAnnotationIcon(parent.context))

    override fun bindView(
        coordinateAnnotationViewHolder: CoordinateAnnotationViewHolder,
        element: StableIdAttraction,
    ) {
        val icon = coordinateAnnotationViewHolder.view as LabeledAnnotationIcon
        icon.labelTextColor = ColorStateList.valueOf(Color.BLACK)
        icon.labelText = "${element.name}\n${element.waitTimeMinutes} min"
        (icon.annotationIcon as ImageView).setImageResource(element.iconImage)
    }

    override fun annotationLocation(element: StableIdAttraction) = GeographicCoordinate(element.latitude, element.longitude)

    // When stable ids are enabled, `replace()` recognises an attraction with the same id as the
    // same annotation and rebinds the live view in place — wait-time labels update without
    // flicker and selection state is preserved. When disabled, the layer falls back to default
    // `T`-equality diffing, so each refresh unmounts every annotation and remounts a fresh one.
    override fun elementStableId(element: StableIdAttraction): Any? = element.id
}
