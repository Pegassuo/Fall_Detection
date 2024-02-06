package com.example.falldetection.presentation

import kotlinx.serialization.Serializable

@Serializable
data class DataContact (
    val name: String,
    val number: String
)