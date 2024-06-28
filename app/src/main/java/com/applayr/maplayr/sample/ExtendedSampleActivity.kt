package com.applayr.maplayr.sample

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import com.applayr.maplayr.MapView
import com.applayr.maplayr.androidLayer.annotation.CoordinateAnnotationLayer
import com.applayr.maplayr.model.coordinate.GeographicCoordinate
import com.applayr.maplayr.model.map.Map
import com.applayr.maplayr.model.opengl.locationmarker.LocationMarker
import com.applayr.maplayr.model.opengl.shapes.Shape
import com.applayr.maplayr.sample.data.annotationlayer.AnnotationLayerAdapter
import com.applayr.maplayr.sample.data.model.Attraction
import com.applayr.maplayr.sample.data.model.AttractionManager
import com.google.android.gms.location.*

class ExtendedSampleActivity : AppCompatActivity() {

    private var mapViewVariable: MapView? = null

    private val mapView: MapView
        get() = mapViewVariable ?: throw Exception("Map view not initialised")

    // Location marker
    private lateinit var locationMarker: LocationMarker

    // Location updates
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var currentLocation: Location? = null

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

        mapView.mapContextLiveData.observe(mapView) { mapContext ->
            mapContext ?: return@observe

            mapView.shapes = listOf()

            coordinateAnnotationLayer.listener = object : CoordinateAnnotationLayer.Listener<Attraction> {

                override fun didDeselectAnnotation(
                    element: Attraction,
                    coordinateAnnotationLayer: CoordinateAnnotationLayer<Attraction>
                ) {
                    mapView.shapes = listOf()
                }

                override fun didSelectAnnotation(
                    element: Attraction,
                    coordinateAnnotationLayer: CoordinateAnnotationLayer<Attraction>
                ) {

                    /* ---------- ---------- ---------- ---------- ---------- */
                    /* -- Route Calculation -- */
                    /* ---------- ---------- ---------- ---------- ---------- */

                    currentLocation?.let { currentLocation ->

                        if (mapContext.isLocationInBounds(currentLocation)) {

                            // Calculate the route from the user's current location to the selected element
                            val route = mapContext.pathNetwork?.calculateDirections(
                                GeographicCoordinate(currentLocation.latitude, currentLocation.longitude),
                                listOf(GeographicCoordinate(element.latitude, element.longitude))
                            )

                            // If the route is available add an outlined shape representing the route to the map and move the camera
                            route?.let { route ->
                                // Add shapes to the map
                                val outline = Shape(route.path, Color.BLUE, 4f)
                                val inner = Shape(route.path, Color.WHITE, 2f)

                                mapView.shapes = listOf(outline, inner)

                                // Animate the camera
                                // Compute the smallest circle that covers the start coordinate, the destination coordinate and all the points along the route
                                val pointsAlongRoute = route.pointsAlongRoute.map { mapPoint ->
                                    mapContext.mapProjection.mapPointToGeographic(mapPoint)
                                }

                                val enclosingCircle = mapView.computeSmallestCircle(
                                    pointsAlongRoute + listOf(
                                        GeographicCoordinate(
                                            currentLocation.latitude,
                                            currentLocation.longitude
                                        ), // Start of the route
                                        GeographicCoordinate(
                                            element.latitude,
                                            element.longitude
                                        ), // Destination of the route
                                    )
                                )

                                // Set the camera position to view the route
                                mapView.moveCamera(
                                    enclosingCircle?.center,
                                    null,
                                    enclosingCircle?.span,
                                    0.0,
                                    Math.toRadians(30.0),
                                    true
                                )
                            }
                        } else {
                            // Show an off resort error
                            AlertDialog.Builder(this@ExtendedSampleActivity)
                                .setTitle("Error")
                                .setMessage("Unable to create route whilst you're not on resort")
                                .setNeutralButton("Ok", null)
                                .show()

                        }
                    }
                }
            }
        }

        // Add the CoordinateAnnotationLayer to the map view
        mapView.addMapLayer(coordinateAnnotationLayer)

        /* ---------- ---------- ---------- ---------- ---------- */
        /* -- Location marker -- */
        /* ---------- ---------- ---------- ---------- ---------- */

        // Create a location marker and add it to the map view
        locationMarker = LocationMarker()

        mapView.addLocationMarker(locationMarker)

        /* -- Location permissions & location updates -- */
        // Get the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check to see if the user has granted at least the COARSE_LOCATION permission (approximate location)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request foreground location permissions
            val locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
                    setUpLocationClient()
                }
            }

            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
        } else {
            setUpLocationClient()
        }

        /* ---------- ---------- ---------- ---------- ---------- */
        /* -- Initial Camera Position -- */
        /* ---------- ---------- ---------- ---------- ---------- */

        // Compute the smallest circle that covers all of the thrill attractions
        val enclosingCircle = mapView.computeSmallestCircle(AttractionManager.thrillAttractions.map { attraction ->
            GeographicCoordinate(attraction.latitude, attraction.longitude)
        })

        // Set the initial camera position to view all of the thrill attractions
        mapView.moveCamera(enclosingCircle?.center, null, enclosingCircle?.span, 0.0, Math.toRadians(45.0), false)

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

    override fun onPause() {
        super.onPause()
        // Stop requesting location updates
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        // Start requesting location updates
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission") // This will only be called if foreground location permissions have been approved
    private fun setUpLocationClient() {
        // Request high accuracy location updates every second
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1_000).build()

        // Assign the callback. This is triggered when the location updates so we can update the locationMarker's location.
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    currentLocation = location
                    locationMarker.location = currentLocation
                }
            }
        }

        fusedLocationClient?.let { locationClient ->
            // Get the last location to initially set the locationMarker's position
            locationClient.lastLocation.addOnSuccessListener { location: Location? ->
                currentLocation = location
                locationMarker.location = currentLocation
            }
        }
    }

    @SuppressLint("MissingPermission") // This will only be called if foreground location permissions have been approved
    private fun startLocationUpdates() {
        locationRequest?.let { request ->
            locationCallback?.let { callback ->
                // Request location updates with the request and callback above
                fusedLocationClient?.requestLocationUpdates(request, callback, Looper.getMainLooper())
            }
        }
    }

    private fun stopLocationUpdates() {
        locationCallback?.let { callback ->
            fusedLocationClient?.removeLocationUpdates(callback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapViewVariable = null
    }
}
