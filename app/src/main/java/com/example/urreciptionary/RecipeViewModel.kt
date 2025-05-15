package com.example.urreciptionary

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urreciptionary.network.ApiService
import com.example.urreciptionary.network.IngredientSuggestion
import com.example.urreciptionary.network.Recipe
import com.example.urreciptionary.network.RecipeResponse
import kotlinx.coroutines.launch

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RecipeViewModel(private val apiService: ApiService) : ViewModel() {
    private var currentPage = 1
    private val pageSize = 10

    private val _ingredientSuggestions = MutableLiveData<List<IngredientSuggestion>>()
    val ingredientSuggestions: LiveData<List<IngredientSuggestion>> = _ingredientSuggestions


//    private val _recipes = MutableLiveData<List<RecipeResponse.Recipe>>()
//    val recipes: LiveData<List<RecipeResponse.Recipe>> get() = _recipes
    private val _recipes = MutableLiveData<List<Recipe>>()
    val recipes: LiveData<List<Recipe>> get() = _recipes



    private val offset = currentPage * pageSize
    val api_key = BuildConfig.API_KEY
    fun searchRecipesByIngredients(ingredients: List<String>) {
        val api_key = BuildConfig.API_KEY
        val ingredientsString = ingredients.joinToString(",")
//        apiService.findRecipesByIngredients(ingredientsString, 10, api_key)
//            .enqueue(object : Callback<List<RecipeResponse>> {
//                override fun onResponse(call: Call<List<RecipeResponse>>, response: Response<List<RecipeResponse>>) {
//                    if (response.isSuccessful) {
//                        val recipeResponseList = response.body()
//                        if (!recipeResponseList.isNullOrEmpty()) {
//                            // Map RecipeResponse.Recipe to Recipe
//                            val recipesList = recipeResponseList.flatMap { it.recipes?.map { recipeResponse ->
//                                Recipe(
//                                    id = recipeResponse.id,
//                                    title = recipeResponse.title,
//                                    image = recipeResponse.image,
//                                    usedIngredientCount = 0, // Update accordingly, depending on how you want to handle this field
//                                    missedIngredientCount = 0, // Update accordingly
//                                    missedIngredients = emptyList(), // Update accordingly
//                                    usedIngredients = emptyList(), // Update accordingly
//                                    likes = 0 // Update accordingly
//                                )
//                            } ?: emptyList() }
//
//                            Log.d("RecipeViewModel", "Received ${recipesList.size} recipes.")
//                            _recipes.postValue(recipesList)
//                        } else {
//                            Log.d("RecipeViewModel", "No recipes found.")
//                            _recipes.postValue(emptyList())
//                        }
//                    } else {
//                        Log.d("RecipeViewModel", "Error fetching recipes: ${response.errorBody()}")
//                        _recipes.postValue(emptyList())
//                    }
//                }
//
//                override fun onFailure(call: Call<List<RecipeResponse>>, t: Throwable) {
//                    Log.d("RecipeViewModel", "Error: $t")
//                    _recipes.postValue(emptyList())
//                }
//            })
    }
}