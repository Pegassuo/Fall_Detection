package com.example.falldetection.presentation

import kotlinx.serialization.Serializable

@Serializable
data class DataFall (
    val id: Int,
    val ubicacion: String,
    val fecha: String,
    val hora: String
)