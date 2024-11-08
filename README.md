# MapLayr

MapLayr for Android is an SDK for displaying interactive maps. It is written in Kotlin and uses a combination of OpenGLES and Canvas for rendering.

&nbsp;

## Installation

### Gradle Plugin

#### Add GitHub Package Registry

##### Create GitHub token

GitHub package registry requires authentication credentials even for public reposistories so these must be created by navigating to here: https://github.com/settings/tokens/new

You will need to enable the following scope on the GitHub token creation form:
```
read:packages
```

##### Add GitHub Package Registry to project Gradle

Add the GitHub package registry to the top level `build.gradle` as so:
```Groovy
buildscript {
    repositories {
        maven {
            url = "https://maven.pkg.github.com/applayr/MapLayr-Android"
            credentials {
                username "GitHub username here"
                password "Github token with packages read access here"
            }
        }
    }
}
```

<details>
    
<summary>Use environment variables for credential storage</summary>    

To avoid saving your Github credentials directly in your repo you can add them to your environment variables. They can they be accessed like so:
```Groovy
buildscript {
    repositories {
        maven {
            url = "https://maven.pkg.github.com/applayr/MapLayr-Android"
            credentials {
                username System.getenv("GITHUB_USERNAME")
                password System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

</details>

An example can be found here: [build.gradle](build.gradle#L9-L15)

##### Add MapLayr Plugin class path to Gradle

Add the MapLayr plugin class path to Gradle:
```Groovy
buildscript {
    dependencies {
        classpath 'com.applayr:maplayr-plugin:0.0.51'
    }
}
```

An example can be found here: [build.gradle](build.gradle#L18)

##### Add MapLayr Plugin to Gradle

Add the plugin to Gradle:
```Groovy
plugins {
    id 'com.applayr.maplayr-plugin'
}
```
An example can be found here: [build.gradle](app/build.gradle#L3)

<details>
    
<summary>Old Gradle syntax</summary>    

```Groovy
apply plugin: 'com.applayr.maplayr-plugin'
```

An example can be found here: [build.gradle](app/build.gradle#L3)

</details>

An example can be found here: [build.gradle](app/build.gradle#L3)

##### Use MapLayr Plugin

```Groovy
appLayr {
    apiKey = "{api_key}"
    mapId = "{map_id}"
}
```

An example can be found here: [build.gradle](app/build.gradle#L61-L72)

<details>
    
<summary>Set cache time</summary>    

The maps are cached for a default period of 24 hours but this can be modified. For example, a build system could have this set to `0` so that the latest map is downloaded for every build.

```Groovy
appLayr {
    cacheMapsForHours = 24
}
```

</details>

<details>
    
<summary>Specify property for variants, buildTypes or product flavors</summary>    

Any of the properties can be nested in the variants, buildTypes or product flavors namespace to make them specific to that build.

```Groovy
appLayr {
    variants {
        variant1 {
            mapId = "{map_id}"
        }
        variant2 {
            mapId = "{map_id}"
        }
    }
    buildTypes {
        buildType1 {
            mapId = "{map_id}"
        }
        buildType2 {
            mapId = "{map_id}"
        }
    }
    productFlavors {
        productFlavor1 {
            mapId = "{map_id}"
        }
        productFlavor2 {
            mapId = "{map_id}"
        }
    }
}
```

</details>

### MapLayr Implementation

##### Create GitHub token

[Same as this token](README.md##create-github-token)

##### Add GitHub Package Registry to module Gradle

Add the GitHub package registry to the module level `build.gradle` as so:
```Groovy
repositories {
    maven {
        url = "https://maven.pkg.github.com/applayr/MapLayr-Android"
        credentials {
            username "GitHub username here"
            password "Github token with packages read access here"
        }
    }
}
```

An example can be found here: [build.gradle](app/build.gradle#L10-L16)

##### Add MapLayr implementation to Gradle module dependencies

```Groovy
dependencies {
    implementation "com.applayr:maplayr:0.0.51"
}
```

An example can be found here: [build.gradle](app/build.gradle#L90)

&nbsp;

## Usage

*Note that in the preview release the API is subject to change*

MapLayr provides the class `MapView`, a subclass of `View`. It can be configured with a `Map` instance, which itself is configured from the map bundled in your app.

&nbsp;

### Displaying a Map

To display an interactive map on the screen, you must first get a managed `Map` instance by specifying your map ID. The map must be located in the correct place in your assets directory (see above).

```Kotlin
try {
    val map = Map.managed(applicationContext, "5a1400cf-db2b-4dec-90f2-8f603cab4e72")

    mapView.map = map

} catch (e: MapBundleException) {
    Log.e("MapBundle", "${e.message}")
}
```

Getting a "managed" map instance rather than instantiating a `Map` yourself allows you to request to check for map updates. The `Map.checkForUpdates()` function can be called to check for updates to all currently loaded managed maps. Any updates will be applied automatically to the necessary `MapView`.

&nbsp;

## Showing Annotations

To show annotations you must add a `CoordinateAnnotationLayer` to the `MapView`. The `CoordinateAnnotationLayer` takes as parameters the `Context` and a `CoordinateAnnotationLayer.Adapter`. The `CoordinateAnnotationLayer` (and `CoordinateAnnotationLayer.Adapter`) is specialised with the type of annotation, which is added to it. Instances of this specialised type can then be inserted into the `CoordinateAnnotationLayer`.

For example, given the following `PointOfInterest` class

```Kotlin
class PointOfInterest (
    val name: String,
    val latitude: Double,
    val longitude: Double
)
```

A `CoordinateAnnotationLayer.Adapter` can be defined as follows

```Kotlin
class AnnotationLayerAdapter: CoordinateAnnotationLayer.Adapter<PointOfInterest>() {

    // Define a CoordinateAnnotationViewHolder containing a LabeledAnnotationIcon
    class LabeledAnnotationIconViewHolder(view: LabeledAnnotationIcon): 
        CoordinateAnnotationViewHolder(view)

    // Create an instance of the LabeledAnnotationIconViewHolder
    override fun createView(parent: ViewGroup, viewType: Int) = 
        LabeledAnnotationIconViewHolder(LabeledAnnotationIcon(parent.context))

    // Bind the data for a given `PointOfInterest` to the LabeledAnnotationIcon
    override fun bindView(
        coordinateAnnotationViewHolder: CoordinateAnnotationViewHolder, 
        element: PointOfInterest
    ) {

        val labeledAnnotationIcon = 
            coordinateAnnotationViewHolder.view as LabeledAnnotationIcon

        labeledAnnotationIcon.annotationLabel.text = element.name

        labeledAnnotationIcon.annotationIcon.setImageResource(R.drawable.poi_icon)

    }

    // Provide the geographic coordinate for a given `PointOfInterest`
    override fun annotationLocation(element: PointOfInterest) = GeographicCoordinate(element.latitude, element.longitude)

}
```

Next, create a `CoordinateAnnotationLayer` and insert the `PointOfInterest` items into it.

```Kotlin
val coordinateAnnotationLayer = CoordinateAnnotationLayer(context, AnnotationLayer()).apply {
    insert(allPointsOfInterest)
}
```

Finally, add the layer to the `MapView`

```Kotlin
mapView.addMapLayer(coordinateAnnotationLayer)
```

&nbsp;

## Showing the User's location

MapLayr includes the class `LocationMarker` whose instances can be added to the `MapView` to display the user's location

```Kotlin
locationMarker = LocationMarker()

mapView.addLocationMarker(locationMarker)
```

The location marker has a `location: Location` property, which can be used to update the position and/or heading. The client application should request the necessary location permissions from the user and then if required listen for location updates to update the position and/or heading of the location marker. 

The sample application includes an example of requesting foreground location permissions and using the `FusedLocationProviderClient` from the Google Play Services library to receive location updates.

&nbsp;

## Calculating & Showing Routes

MapLayr can calculate routes between two points and display them on the map.

You can use the `PathNetwork` instance supplied by the `Map` to calculate the route from one location to another. In the code snippet below `currentLocation` is of type `Location` discussed in the 'Showing the User's location' section.

```Kotlin
val userLocation = GeographicCoordinate(currentLocation.latitude, currentLocation.longitude)
val destinationLocation = GeographicCoordinate(52.8952, -1.8431)

val route = map.pathNetwork.calculateDirections(
    from = userLocation,
    to = destinationLocation
)
```

Multiple possible endpoints can be supplied, for example if a destination had multiple entrances—the resulting route will use the closest destination. Additionally, calculateDirections takes an optional final argument to specify route options, such as avoiding path segments flagged as unsuitable for wheelchairs & pushchairs.

The returned `Route` has properties for its total distance and a `Path` instance, which can be added to the map view as a shape:

```kotlin
 val shape = Shape(
    path = route.path, 
    strokeColor = Color.BLUE, 
    strokeWidth = 4f
)
mapView.shapes = listOf(shape)
```

&nbsp;

## Setting the Camera

It is often desirable to zoom the map to a certain position, or reveal all the annotations, etc. The map view contains a method, moveCamera, which allows changing the projection, optionally with an animation:

```kotlin
mapView.moveCamera(
    coordinates = GeographicCoordinate(52.8952, -1.8431),
    headingDegrees = 45.0,
    span = 50.0,
    insets = 0.0,
    tilt = Math.toRadians(45.0),
    animated = true
)
```

All arguments are optional and, left out, preserve the camera's current state.

A function which calculates the smallest enclosing circle for a number of coordinates is included in the SDK to make determining the coordinates and span to provide easier:

```kotlin
val enclosingCircle = mapView.computeSmallestCircle(
    coordinates = allPointsOfInterest.map { pointOfInterest ->
        GeographicCoordinate(pointOfInterest.latitude.toDouble(), pointOfInterest.longitude.toDouble())
    }
)

mapView.moveCamera(
    coordinates = enclosingCircle?.center,
    span = enclosingCircle?.span,
    insets = 50.0,
    animated = true
)
```

*Note: enclosingCircle will only be null if the `Map` instance has not been set on the `MapView`*

&nbsp;

## Safe Area Insets

By default the camera is positioned over the centre of the screen. If there are views that obstruct the map view you may wish to have the camera positioned in the centre of the area not obstructed by other views, this is the safe area. 

The safe area insets are specified as pixel insets from the top, left, right & bottom edges of the view.

For example if you have a map view where the bottom half is obstructed by another view you can specify the safe area insets as follows

```kotlin
mapView.safeAreaInsets = Rect(0, 0, 0, mapView.height / 2) 
```

If you were then to move the camera to focus on a given coordinate, this coordinate would be displayed in the centre of the top half of the screen.

&nbsp;

## Miscellaneous

### Map Limit

MapLayr uses OpenGLES to render the interactive map. Some devices have a limit on the number of `EGLContext` objects that are allowed at a point in time and thus after this point maps will not display. As an example the Google Pixel 2 limits the number to 32 maps on a single screen, whereas a Samsung Galaxy S10+ doesn't seem to have a limit.
