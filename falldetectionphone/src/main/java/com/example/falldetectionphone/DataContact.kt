package com.example.falldetectionphone

import kotlinx.serialization.Serializable

@Serializable
data class DataContact (
    val name: String,
    val number: String
)