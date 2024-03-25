package com.example.falldetection

import kotlinx.serialization.Serializable

@Serializable
data class DataContact (
    val name: String,
    val number: String
)