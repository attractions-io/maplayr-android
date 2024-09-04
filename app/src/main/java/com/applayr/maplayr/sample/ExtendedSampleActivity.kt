package com.applayr.maplayr.sample

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import com.applayr.maplayr.CameraTarget
import com.applayr.maplayr.MapView
import com.applayr.maplayr.androidLayer.annotation.CoordinateAnnotationLayer
import com.applayr.maplayr.location.google.GoogleLocationId
import com.applayr.maplayr.model.coordinate.GeographicCoordinate
import com.applayr.maplayr.model.coordinate.DestinationProvider
import com.applayr.maplayr.model.map.Map
import com.applayr.maplayr.model.opengl.journey.Journey
import com.applayr.maplayr.model.opengl.locationmarker.LocationMarker
import com.applayr.maplayr.model.opengl.route.AnimatingShapedRoute
import com.applayr.maplayr.model.opengl.shapes.Shape
import com.applayr.maplayr.model.routes.AnimatingRoute
import com.applayr.maplayr.sample.data.annotationlayer.AnnotationLayerAdapter
import com.applayr.maplayr.sample.data.model.Attraction
import com.applayr.maplayr.sample.data.model.AttractionManager
import kotlin.math.PI

class ExtendedSampleActivity : AppCompatActivity() {

    private var mapViewVariable: MapView? = null

    private val mapView: MapView
        get() = mapViewVariable ?: throw Exception("Map view not initialised")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_extended_sample)

        mapViewVariable = findViewById(R.id.map_view)

        /* ---------- ---------- ---------- ---------- ---------- */
        /* -- Map Initialisation -- */
        /* ---------- ---------- ---------- ---------- ---------- */

        // Load the map metadata and assign it to the map view
        mapView.setMap(Map.managed(applicationContext, BuildConfig.MAPLAYR_MAP))

        /* ---------- ---------- ---------- ---------- ---------- */
        /* -- Annotation layer -- */
        /* ---------- ---------- ---------- ---------- ---------- */

        val annotationLayerAdapter = AnnotationLayerAdapter()

        // Create the CoordinateAnnotationLayer and add the annotations to it
        val coordinateAnnotationLayer = CoordinateAnnotationLayer(this, annotationLayerAdapter).apply {
            insert(AttractionManager.thrillAttractions)
        }

        coordinateAnnotationLayer.listener = object : CoordinateAnnotationLayer.Listener<Attraction> {

            override fun didDeselectAnnotation(
                element: Attraction,
                coordinateAnnotationLayer: CoordinateAnnotationLayer<Attraction>
            ) {
                mapView.removeAllJourneys()
            }

            override fun didSelectAnnotation(
                element: Attraction,
                coordinateAnnotationLayer: CoordinateAnnotationLayer<Attraction>
            ) {

                /* ---------- ---------- ---------- ---------- ---------- */
                /* -- Route Calculation -- */
                /* ---------- ---------- ---------- ---------- ---------- */

                mapView.removeAllJourneys()

                val journey = Journey(
                    from = DestinationProvider(GoogleLocationId.HighAccuracy),
                    to = listOf(DestinationProvider(GeographicCoordinate(element.latitude, element.longitude))),
                    options = Journey.Options(
                        listener = object : (AnimatingRoute?) -> Unit {

                            private var shownError = false
                            private var readyToShowError = true

                            override fun invoke(animatingRoute: AnimatingRoute?) {
                                if (animatingRoute == null) {
                                    if (!shownError) {
                                        shownError = true
                                        if (readyToShowError) {
                                            readyToShowError = false
                                            // Show an off resort error
                                            AlertDialog.Builder(this@ExtendedSampleActivity)
                                                .setTitle("Error")
                                                .setMessage("Unable to create route whilst you're not on resort")
                                                .setNeutralButton("Ok") { _, _ -> readyToShowError = true }
                                                .show()
                                        }
                                    }
                                } else {
                                    shownError = false
                                }
                            }
                        },
                        animatingRouteShaper = { animatingRoute ->
                            AnimatingShapedRoute(
                                animatingRoute = animatingRoute,
                                shapes = listOf(
                                    Shape(animatingRoute.route.path, Color.BLUE, 4f),
                                    Shape(animatingRoute.route.path, Color.WHITE, 2f)
                                )
                            )
                        }
                    )
                )
                mapView.addJourney(journey)
                mapView.moveCamera(
                    cameraTarget = CameraTarget.Journey(
                        journey = journey,
                        tilt = Math.toRadians(30.0),
                        fallback = CameraTarget.Stationary
                    ),
                    animated = true
                )
            }
        }

        // Add the CoordinateAnnotationLayer to the map view
        mapView.addMapLayer(coordinateAnnotationLayer)

        /* ---------- ---------- ---------- ---------- ---------- */
        /* -- Location marker -- */
        /* ---------- ---------- ---------- ---------- ---------- */

        // Create a location marker and add it to the map view
        val locationMarker = LocationMarker(GoogleLocationId.HighAccuracy)

        mapView.addLocationMarker(locationMarker)

        // Check to see if the user has granted at least the COARSE_LOCATION permission (approximate location)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }

        /* ---------- ---------- ---------- ---------- ---------- */
        /* -- Initial Camera Position -- */
        /* ---------- ---------- ---------- ---------- ---------- */

        // Set the initial camera position to current user location and fallback to view all of the thrill attractions
        mapView.moveCamera(
            CameraTarget.LocationId(
                locationId = GoogleLocationId.HighAccuracy,
                tilt = PI / 4,
                span = 75.0,
                fallback = CameraTarget.ResolvableDestinations(
                    geographicCoordinateProviders = AttractionManager.thrillAttractions.map { attraction -> GeographicCoordinate(attraction.latitude, attraction.longitude) },
                    headingDegrees = Math.toRadians(45.0),
                    insets = 0.0,
                    tilt = 0.0,
                )
            ),
            animated = false
        )

        /* ---------- ---------- ---------- ---------- ---------- */
        /* -- Safe Area Insets -- */
        /* ---------- ---------- ---------- ---------- ---------- */

        val centreOnDaeva = false
        val useSafeAreaInsets = false

        findViewById<TextView>(R.id.text_view).visibility = if (useSafeAreaInsets) View.VISIBLE else View.GONE

        mapView.doOnLayout {

            if (useSafeAreaInsets) {
                mapView.safeAreaInsets = Rect(0, 0, 0, mapView.height / 2)
            }

            if (centreOnDaeva) {
                val daeva = AttractionManager.thrillAttractions[0]

                mapView.moveCamera(
                    coordinates = GeographicCoordinate(daeva.latitude, daeva.longitude),
                    span = 0.0
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapViewVariable = null
    }
}
