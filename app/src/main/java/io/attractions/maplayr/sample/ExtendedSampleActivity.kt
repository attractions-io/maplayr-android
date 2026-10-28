package io.attractions.maplayr.sample

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
import io.attractions.livedata.LifecycleOwner
import io.attractions.maplayr.CameraTarget
import io.attractions.maplayr.MapView
import io.attractions.maplayr.androidLayer.annotation.CoordinateAnnotationLayer
import io.attractions.maplayr.model.coordinate.DestinationProvider
import io.attractions.maplayr.model.map.Map
import io.attractions.maplayr.model.opengl.locationmarker.LocationMarker
import io.attractions.maplayr.model.opengl.shapes.Shape
import io.attractions.maplayr.model.routes.RouteResult
import io.attractions.maplayr.model.routes.path
import io.attractions.maplayr.sample.data.annotationlayer.AnnotationLayerAdapter
import io.attractions.maplayr.sample.data.model.Attraction
import io.attractions.maplayr.sample.data.model.AttractionManager
import io.attractions.positioning.google.GoogleLocationId
import io.attractions.positioning.model.coordinate.GeographicCoordinate
import kotlin.math.PI

class ExtendedSampleActivity : AppCompatActivity() {

    private var mapViewVariable: MapView? = null

    private val routeLifecycleOwner = LifecycleOwner()

    private var routeShapes = emptyList<Shape>()

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
        val map = Map.managed(BuildConfig.MAPLAYR_MAP)

        mapView.setMap(map)

        /* ---------- ---------- ---------- ---------- ---------- */
        /* -- Annotation layer -- */
        /* ---------- ---------- ---------- ---------- ---------- */

        val annotationLayerAdapter = AnnotationLayerAdapter()

        // Create the CoordinateAnnotationLayer and add the annotations to it
        val coordinateAnnotationLayer = CoordinateAnnotationLayer(this, annotationLayerAdapter).apply {
            insert(AttractionManager.attractions)
        }

        coordinateAnnotationLayer.listener = object : CoordinateAnnotationLayer.Listener<Attraction> {

            override fun didDeselectAnnotation(
                element: Attraction,
                coordinateAnnotationLayer: CoordinateAnnotationLayer<Attraction>
            ) {
                routeLifecycleOwner.cancelAll()
                routeShapes.forEach { routeShape -> mapView.removeShape(routeShape) }
                routeShapes = emptyList()
            }

            override fun didSelectAnnotation(
                element: Attraction,
                coordinateAnnotationLayer: CoordinateAnnotationLayer<Attraction>
            ) {

                /* ---------- ---------- ---------- ---------- ---------- */
                /* -- Route Calculation -- */
                /* ---------- ---------- ---------- ---------- ---------- */

                routeLifecycleOwner.cancelAll()

                val route = map.createRoute(
                    from = DestinationProvider(GoogleLocationId.HighAccuracy),
                    to = listOf(DestinationProvider(GeographicCoordinate(element.latitude, element.longitude)))
                )

                route.observe(routeLifecycleOwner) { routeResult ->
                    if (routeResult is RouteResult.Failure.OutOfBounds) {
                        AlertDialog.Builder(this@ExtendedSampleActivity)
                            .setTitle("Error")
                            .setMessage("Unable to create route whilst you're not on resort")
                            .setNeutralButton("Ok", null)
                            .show()
                    }
                }

                routeShapes.forEach { routeShape -> mapView.removeShape(routeShape) }
                routeShapes = listOf(
                    Shape(route.path, Color.BLUE, 4f),
                    Shape(route.path, Color.WHITE, 2f)
                )
                routeShapes.forEach { routeShape -> mapView.addShape(routeShape) }
                mapView.moveCamera(
                    cameraTarget = CameraTarget.Route(
                        route = route,
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
                    geographicCoordinateProviders = AttractionManager.attractions.map { attraction ->
                        GeographicCoordinate(attraction.latitude, attraction.longitude)
                    },
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
                val daeva = AttractionManager.attractions[0]

                mapView.moveCamera(
                    coordinates = GeographicCoordinate(daeva.latitude, daeva.longitude),
                    span = 0.0
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        routeLifecycleOwner.cancelAll()
        mapViewVariable = null
    }
}
