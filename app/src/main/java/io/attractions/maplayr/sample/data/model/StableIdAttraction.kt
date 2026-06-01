package io.attractions.maplayr.sample.data.model

data class StableIdAttraction(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val iconImage: Int,
    val waitTimeMinutes: Int,
)
