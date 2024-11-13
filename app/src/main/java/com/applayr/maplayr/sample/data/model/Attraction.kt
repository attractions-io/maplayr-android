package com.applayr.maplayr.sample.data.model

data class Attraction(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val iconImage: Int
) {

    val queueTimeHandler = QueueTimeHandler()

    override fun toString() = name
}
