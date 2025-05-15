package com.example.urreciptionary

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.urreciptionary.network.ApiService
import com.example.urreciptionary.network.IngredientSuggestion
import com.example.urreciptionary.network.RecipeDetailsResponse
import com.example.urreciptionary.network.RecipeResponse
import com.example.urreciptionary.network.RetrofitClient
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CookingActivity : BaseActivity() {

    private lateinit var ingredientsAndButtonContainer: LinearLayout
    private lateinit var clearAllButton: MaterialButton

    //Search Bar Suggestions
    private lateinit var searchBarSuggestionRecyclerView: RecyclerView
    private lateinit var ingredientAdapter: CookingIngredientAdapter
    private val ingredientSuggestions = mutableListOf<IngredientSuggestion>()

    private lateinit var ingredientInput: AutoCompleteTextView
    private lateinit var suggestionRecyclerView: RecyclerView
    private lateinit var recipeAdapter: CookingRecipeAdapter
    private var recipeList = mutableListOf<RecipeResponse.Recipe>()
    private lateinit var ingredientsScrollView: HorizontalScrollView

    private var selectedSortDirection: String? = null

    private lateinit var popularIngredientChipGroup: ChipGroup
    private val popularIngredients = listOf("Rice", "Yoghurt", "Chicken")

    private val addedIngredients = mutableListOf<String>()

    private val apiService: ApiService by lazy {
        RetrofitClient.getApiService()
    }

    private val apiKey = BuildConfig.API_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setActivityLayout(R.layout.activity_cooking)
        initializeBottomNavigation()
        setupBackButton()

        ingredientInput = findViewById(R.id.ingredientInput)
        popularIngredientChipGroup = findViewById(R.id.popularIngredientChipGroup)

        showPopularIngredientsChips()

        suggestionRecyclerView = findViewById(R.id.suggestionRecyclerView)
        suggestionRecyclerView.visibility = View.VISIBLE
        ingredientsScrollView = findViewById(R.id.ingredientsScrollView)
        clearAllButton = findViewById(R.id.btnClear)
        ingredientsAndButtonContainer = findViewById(R.id.ingredientsAndButtonContainer)

        suggestionRecyclerView.layoutManager = LinearLayoutManager(this)
        val suggestionText = findViewById<TextView>(R.id.suggestionText)
        recipeAdapter = CookingRecipeAdapter(recipeList)
        suggestionRecyclerView.adapter = recipeAdapter

        searchBarSuggestionRecyclerView = findViewById(R.id.searchBarSuggestionRecyclerView)
        ingredientAdapter = CookingIngredientAdapter(ingredientSuggestions) { ingredient ->
            val currentText = ingredientInput.text.toString().trim()

            val words = currentText.split(",").map { it.trim() }

            val updatedWords = words.dropLast(1) + ingredient

            val newText = updatedWords.joinToString(", ")

            ingredientInput.setText(newText)
            ingredientInput.setSelection(newText.length)

            searchBarSuggestionRecyclerView.visibility = View.GONE
        }
        searchBarSuggestionRecyclerView.adapter = ingredientAdapter

        ingredientInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    searchBarSuggestionRecyclerView.visibility = View.GONE
                    suggestionText.visibility = View.GONE
                } else {
                    searchBarSuggestionRecyclerView.visibility = View.VISIBLE
                    suggestionText.visibility = View.VISIBLE
                    getIngredientSuggestions(s.toString())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        findViewById<MaterialButton>(R.id.submitButton).setOnClickListener {
            val ingredients = ingredientInput.text.toString().trim()
            if (ingredients.isNotEmpty()) {
                addIngredientToList(ingredients)
                saveRecentSearch(this, ingredients)
                clearAllButton.visibility = View.VISIBLE
                ingredientsAndButtonContainer.visibility = View.VISIBLE
                ingredientInput.text.clear()
                showRecentSearches()
            } else {
                warningToast(this, "Please enter some ingredients")
            }
        }

        clearAllButton.setOnClickListener {
            clearAllIngredients()
        }

        ingredientInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                searchBarSuggestionRecyclerView.visibility = View.GONE
            }
        }

        val rootLayout: View = findViewById(R.id.rootLayout)
        rootLayout.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                searchBarSuggestionRecyclerView.visibility = View.GONE
                v.performClick()
            }
            false
        }

    }

    private fun saveRecentSearch(context: Context, searchTerm: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("recent_searches", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val recentSearchList = sharedPreferences.getString("recent_searches_list", "")
        val updateSearches = if (recentSearchList.isNullOrEmpty()) {
            searchTerm
        } else {
            val searchList = recentSearchList.split(",").map { it.trim() }.toMutableList()
            if (!searchList.contains(searchTerm)) {
                searchList.add(searchTerm)
            }
            searchList.joinToString(",")
        }
        editor.putString("recent_searches_list", updateSearches)
        editor.apply()
    }

    private fun getRecentSearches(context: Context): List<String> {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("recent_searches", Context.MODE_PRIVATE)
        val recentSearches = sharedPreferences.getString("recent_searches_list", "")
        return if (recentSearches.isNullOrEmpty()) {
            emptyList()
        } else {
            recentSearches.split(",").map { it.trim() }
        }
    }

    private fun showRecentSearches() {
        val recentSearches = getRecentSearches(this)
        val recentIngredientChipGroup: ChipGroup = findViewById(R.id.recentIngredientChipGroup)
        val recentText: TextView = findViewById(R.id.recent_text)
        recentIngredientChipGroup.removeAllViews()

        if (recentSearches.isEmpty()) {
            recentText.visibility = View.GONE
            return
        } else {
            recentText.visibility = View.VISIBLE
        }

        recentSearches.forEach { searchTerm ->
            val chip = Chip(this)
            chip.text = searchTerm
            chip.setChipIconResource(R.drawable.ic_recent)
            val typefaces = ResourcesCompat.getFont(
                this@CookingActivity,
                R.font.bricolage_bold
            )
            chip.isClickable = true

            chip.typeface = typefaces
            chip.setChipBackgroundColorResource(R.color.white)
            chip.setChipStrokeColorResource(R.color.primary_red)
            chip.chipStrokeWidth = 2f
            chip.setTextColor(Color.BLACK)
            chip.isCloseIconVisible = false

            chip.setOnClickListener {
                ingredientInput.setText(searchTerm)
            }

            recentIngredientChipGroup.addView(chip)
        }

        if (recentSearches.isNotEmpty()) {
            val clearAllChip = Chip(this)
            clearAllChip.text = "Clear All"
            val typefaces = ResourcesCompat.getFont(
                this@CookingActivity,
                R.font.bricolage_bold
            )
            clearAllChip.typeface = typefaces
            clearAllChip.setChipIconResource(R.drawable.ic_cross)
            clearAllChip.setTextColor(Color.WHITE)
            clearAllChip.setChipBackgroundColorResource(R.color.primary_red)
            clearAllChip.isClickable = true

            clearAllChip.setOnClickListener {
                clearRecentSearches()
                recentIngredientChipGroup.removeAllViews()
                recentText.visibility = View.GONE
            }

            recentIngredientChipGroup.addView(clearAllChip)
        }
    }

    private fun clearRecentSearches() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("recent_searches", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("recent_searches_list")
        editor.apply()
    }

    private fun showPopularIngredientsChips() {
        popularIngredientChipGroup.removeAllViews()
        popularIngredients.forEach { ingredient ->
            val chip = Chip(this@CookingActivity)
            chip.text = ingredient
            val typefaces = ResourcesCompat.getFont(
                this@CookingActivity,
                R.font.bricolage_bold
            )
            chip.typeface = typefaces

            chip.setChipBackgroundColorResource(R.color.white)
            chip.setChipStrokeColorResource(R.color.primary_red)
            chip.chipStrokeWidth = 2f
            chip.setTextColor(Color.BLACK)
            chip.isCloseIconVisible = false

            chip.setOnClickListener {
                val currentText = ingredientInput.text.toString().trim()
                val newText =
                    if (currentText.isNotEmpty()) "$currentText, $ingredient" else ingredient
                ingredientInput.setText(newText)
                ingredientInput.setSelection(newText.length)

                chip.visibility = View.GONE
            }

            popularIngredientChipGroup.addView(chip)
        }
    }

    private fun addIngredientToList(ingredients: String) {
        if (!addedIngredients.contains(ingredients)) {
            addedIngredients.add(ingredients)
        }
        showIngredientsInScrollView()
        fetchRecipesByIngredients(
            addedIngredients.joinToString(","),
            selectedSortDirection
        )

        findViewById<MaterialButton>(R.id.submitButton).visibility = View.VISIBLE
        findViewById<MaterialButton>(R.id.btnClear).visibility = View.VISIBLE
    }

    private fun showIngredientsInScrollView() {
        val chipGroup: ChipGroup = findViewById(R.id.ingredientsChipGroup)

        chipGroup.removeAllViews()


        val ingredientsList = addedIngredients.joinToString(",").split(",").map { it.trim() }

        ingredientsList.forEach { ingredient ->
            if (ingredient.isNotEmpty()) {
                val chip = Chip(this@CookingActivity)
                val typeface = ResourcesCompat.getFont(this, R.font.bricolage_regular)
                chip.typeface = typeface
                chip.text = ingredient
                chip.isCloseIconVisible = true
                chip.setTextColor(Color.BLACK)
                chip.setChipBackgroundColorResource(R.color.white)
                chip.setChipStrokeColorResource(R.color.primary_red)
                chip.chipStrokeWidth = 2f

                chip.setOnCloseIconClickListener {
                    removeIngredientFromList(ingredient)
                }

                chipGroup.addView(chip)
            }
        }

        if (ingredientsList.isEmpty()) {
            ingredientsScrollView.visibility = View.GONE
        } else {
            ingredientsScrollView.visibility = View.VISIBLE
        }
    }

    private fun clearAllIngredients() {
        addedIngredients.clear()
        ingredientInput.text.clear()
        ingredientsScrollView.visibility = View.GONE

        showPopularIngredientsChips()
        fetchRecipesByIngredients("", "")
    }

    private fun removeIngredientFromList(ingredient: String) {
        addedIngredients.remove(ingredient)
        showIngredientsInScrollView()
        fetchRecipesByIngredients(
            addedIngredients.joinToString(","),
            selectedSortDirection
        )

        if (addedIngredients.isEmpty()) {
            findViewById<MaterialButton>(R.id.btnClear).visibility = View.GONE
        }
    }

    private fun getIngredientSuggestions(query: String) {
        apiService.autocompleteIngredient(query, 4, apiKey).enqueue(object :
            Callback<List<IngredientSuggestion>> {
            override fun onResponse(
                call: Call<List<IngredientSuggestion>>,
                response: Response<List<IngredientSuggestion>>
            ) {
                if (response.isSuccessful) {
                    ingredientSuggestions.clear()

                    response.body()?.let {
                        ingredientSuggestions.addAll(it)
                        ingredientAdapter.notifyDataSetChanged()
                    }

                    searchBarSuggestionRecyclerView.visibility = View.VISIBLE

                } else {
                    errorToast(this@CookingActivity, "Failed to fetch ingredient suggestions")
                }
            }

            override fun onFailure(call: Call<List<IngredientSuggestion>>, t: Throwable) {
                errorToast(this@CookingActivity, "Error: ${t.message}")
            }
        })
    }

    private fun fetchRecipesByIngredients(ingredients: String, sortDirection: String?) {
        if (ingredients.isBlank()) {
            return
        }
        apiService.findRecipesByIngredients(
            ingredients,
            sortDirection,
            4,
            apiKey
        )
            .enqueue(object : Callback<List<RecipeResponse.Recipe>> {
                override fun onResponse(
                    call: Call<List<RecipeResponse.Recipe>>,
                    response: Response<List<RecipeResponse.Recipe>>
                ) {
                    if (response.isSuccessful) {
                        recipeList.clear()
                        val recipes = response.body()
                        recipes?.let {
                            recipeList.addAll(it)
                            recipeAdapter.notifyDataSetChanged()

                            it.forEach { recipe ->
                                fetchRecipesById(recipe.id)
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@CookingActivity,
                            "Failed to fetch recipes",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<RecipeResponse.Recipe>>, t: Throwable) {
                    Toast.makeText(this@CookingActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun fetchRecipesById(recipeId: Int) {
        apiService.getRecipeDetails(recipeId, apiKey)
            .enqueue(object : Callback<RecipeDetailsResponse> {
                override fun onResponse(
                    call: Call<RecipeDetailsResponse>,
                    response: Response<RecipeDetailsResponse>
                ) {
                    if (response.isSuccessful) {
                        val recipeDetails = response.body()
                        recipeDetails?.let {
                            val readyInMinutes = it.readyInMinutes
                            val healthScore = it.healthScore

                            updateRecipeWithDetails(recipeId, readyInMinutes, healthScore)
                        }
                    } else {
                        Toast.makeText(
                            this@CookingActivity,
                            "Failed to fetch recipe details",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<RecipeDetailsResponse>, t: Throwable) {
                    Toast.makeText(this@CookingActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }

            })
    }

    private fun updateRecipeWithDetails(recipeId: Int, readyInMinutes: Int, healthScore: Double) {
        val recipe = recipeList.find { it.id == recipeId }
        recipe?.let {
            it.readyInMinutes = readyInMinutes
            it.healthScore = healthScore
            recipeAdapter.notifyDataSetChanged()
        }
    }
}