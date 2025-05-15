package com.example.urreciptionary.network

import com.google.gson.annotations.SerializedName

data class Recipe(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("image") val image: String,
    @SerializedName("readyInMinutes") val readyInMinutes: Int,
    @SerializedName("healthScore") val healthScore: Double,
    @SerializedName("cookTime") val cookTime: Int,
    @SerializedName("aggregateLikes") val likes: Int
)
