package com.example.urreciptionary.network;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("food/ingredients/autocomplete")
    Call<List<IngredientSuggestion>> autocompleteIngredient(
            @Query("query") String query,
            @Query("number") int number,
            @Query("apiKey") String apiKey
    );

    @GET("recipes/findByIngredients")
    Call<List<RecipeResponse.Recipe>> findRecipesByIngredients(
            @Query("ingredients") String ingredients,
            @Query("sortDirection") String sortDirection,
            @Query("number") int number,
            @Query("apiKey") String apiKey
    );

    @GET("recipes/complexSearch")
    Call<RecipeResponse> getRecipe(
            @Query("query") String query,
            @Query("apiKey") String apiKey,
            @Query("offset") int offset,
            @Query("sort") String sort
    );

    @GET("recipes/complexSearch")
    Call<RecipeResponse> getMealPlannerRecipe(
            @Query("query") String query,
            @Query("apiKey") String apiKey,
            @Query("offset") int offset,
            @Query("sort") String sort,
            @Query("diet") String diet
    );

    @GET("recipes/random")
    Call<List<RecipeResponse.Recipe>> getRandomRecipes(
            @Query("tags") String tags,
            @Query("apiKey") String apiKey
    );

    @GET("recipes/{id}/information")
    Call<RecipeDetailsResponse> getRecipeDetails(
            @Path("id") int id,
            @Query("apiKey") String apiKey
    );

    @GET("recipes/{id}/nutritionWidget.json")
    Call<NutritionWidgetResponse> getNutritionWidget(
            @Path("id") int id,
            @Query("apiKey") String apiKey
    );

    @Headers("Authorization: Key cf0e7dd25d8f4bde945d94e23673f617")
    @POST("v2/models/food-item-recognition/outputs")
    Call<ClarifaiResponse> detectFood(@Body RequestBody requestBody);
}
