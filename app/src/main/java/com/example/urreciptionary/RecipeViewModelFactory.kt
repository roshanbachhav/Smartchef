package com.example.urreciptionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.urreciptionary.network.ApiService


class RecipeViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
            RecipeViewModel(apiService) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}