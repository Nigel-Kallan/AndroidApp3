package com.trios.androidapp3

// Represents one location in the Brampton Treasure Hunt
data class TreasureLocation(
    val name: String,
    val clue: String,
    val latitude: Double,
    val longitude: Double
)