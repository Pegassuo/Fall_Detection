package com.example.falldetection

import kotlinx.serialization.Serializable

@Serializable
data class DataFall (
    var id: Int,
    var latitude: Double,
    var longitude: Double,
    val fecha: String,
    val hora: String,
    val contacts: MutableList<DataContact>?
)