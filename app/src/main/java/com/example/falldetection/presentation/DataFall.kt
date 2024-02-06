package com.example.falldetection.presentation

import kotlinx.serialization.Serializable

@Serializable
data class DataFall (
    var id: Int,
    val ubicacion: String,
    val fecha: String,
    val hora: String
)