package com.example.urreciptionary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.urreciptionary.network.ApiService
import com.example.urreciptionary.network.RecipeDetailsResponse
import com.example.urreciptionary.network.RecipeResponse
import com.example.urreciptionary.network.RecipeWithDetails
import com.example.urreciptionary.network.RetrofitClient

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PopularFoodsViewModel : ViewModel() {

    private val _popularRecipes = MutableLiveData<List<RecipeWithDetails>>()
    val popularRecipes: LiveData<List<RecipeWithDetails>> get() = _popularRecipes

    private val apiService = RetrofitClient.getRetrofitInstance().create(ApiService::class.java)

    var hasLoaded = false
    private var isDataFetching = false

    fun loadPopularRecipe(apiKey: String, page: Int = 0, pageSize: Int = 20) {
        if (hasLoaded || isDataFetching) return

        isDataFetching = true
        val offset = page * pageSize
        apiService.getRecipe("", apiKey, offset, "popularity")
            .enqueue(object : Callback<RecipeResponse> {
                override fun onResponse(
                    call: Call<RecipeResponse>,
                    response: Response<RecipeResponse>
                ) {
                    if (response.isSuccessful) {
                        val recipes = response.body()?.recipes ?: emptyList()
                        if (recipes.isNotEmpty()) {
                            val recipeDetailsList = mutableListOf<RecipeWithDetails>()
                            var loadedCount = 0

                            for (recipe in recipes) {
                                apiService.getRecipeDetails(recipe.id, apiKey)
                                    .enqueue(object : Callback<RecipeDetailsResponse> {
                                        override fun onResponse(
                                            call: Call<RecipeDetailsResponse>,
                                            response: Response<RecipeDetailsResponse>
                                        ) {
                                            response.body()?.let { details ->
                                                recipeDetailsList.add(
                                                    RecipeWithDetails(
                                                        recipe.id,
                                                        recipe.title,
                                                        recipe.image,
                                                        details.readyInMinutes,
                                                        details.likes
                                                    )
                                                )
                                            }
                                            loadedCount++
                                            if (loadedCount == recipes.size) {
                                                _popularRecipes.postValue(recipeDetailsList)
                                                hasLoaded = true
                                                isDataFetching = false
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<RecipeDetailsResponse>,
                                            t: Throwable
                                        ) {
                                            loadedCount++
                                            if (loadedCount == recipes.size) {
                                                _popularRecipes.postValue(recipeDetailsList)
                                                hasLoaded = true
                                                isDataFetching = false
                                            }
                                        }
                                    })
                            }
                        } else {
                            _popularRecipes.postValue(emptyList())
                            isDataFetching = false
                        }
                    } else {
                        _popularRecipes.postValue(emptyList())
                        isDataFetching = false
                    }
                }

                override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                    _popularRecipes.postValue(emptyList())
                    isDataFetching = false
                }
            })
    }

    fun resetLoadState() {
        hasLoaded = false
    }
}