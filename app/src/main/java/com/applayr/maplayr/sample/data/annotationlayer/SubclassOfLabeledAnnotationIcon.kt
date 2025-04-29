package com.applayr.maplayr.sample.data.annotationlayer

import android.content.Context
import androidx.core.view.isVisible
import com.applayr.maplayr.androidLayer.MapLayer
import com.applayr.maplayr.androidLayer.annotation.defaultAnnotation.LabeledAnnotationIcon
import com.applayr.maplayr.context.MapViewFrameContext

class SubclassOfLabeledAnnotationIcon(context: Context) : LabeledAnnotationIcon(context), MapLayer {

    var geographicCoordinate: com.applayr.positionlayr.model.coordinate.GeographicCoordinate? = null

    override fun onDraw(mapViewFrameContext: MapViewFrameContext) {
        val scale = mapViewFrameContext.scaleAt(geographicCoordinate ?: throw IllegalStateException("Geographic coordinate is null"))
        isVisible = scale > MINIMUM_SCALE
    }

    private companion object {
        const val MINIMUM_SCALE = 0.5
    }
}
