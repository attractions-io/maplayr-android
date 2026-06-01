package io.attractions.maplayr.sample.data.model

import io.attractions.maplayr.sample.R
import io.attractions.positioning.model.coordinate.GeographicCoordinate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object StableIdAttractionManager {

    private val seed = listOf(
        Triple("daeva", "Daeva", GeographicCoordinate(52.8952, -1.8431)),
        Triple("eagles-flight", "Eagle's Flight", GeographicCoordinate(52.8982, -1.8463)),
        Triple("terragon", "Terragon", GeographicCoordinate(52.8983, -1.8494)),
        Triple("flying-dutchman", "The Flying Dutchman", GeographicCoordinate(52.8971, -1.8443)),
        Triple("wooden-warrior", "Wooden Warrior", GeographicCoordinate(52.8949, -1.8445)),
    )

    fun snapshot(): Set<StableIdAttraction> = seed.map { (id, name, coordinate) ->
        StableIdAttraction(
            id = id,
            name = name,
            latitude = coordinate.latitude,
            longitude = coordinate.longitude,
            iconImage = R.drawable.map_icon,
            waitTimeMinutes = (0..120).random(),
        )
    }.toSet()

    fun attractions(): Flow<Set<StableIdAttraction>> = flow {
        while (true) {
            emit(snapshot())
            delay(5_000L)
        }
    }
}
