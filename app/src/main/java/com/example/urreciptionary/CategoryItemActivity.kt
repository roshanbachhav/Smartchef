package com.example.urreciptionary

import RecipeAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.urreciptionary.network.ApiService
import com.example.urreciptionary.network.RecipeResponse
import com.example.urreciptionary.network.RetrofitClient
import com.facebook.shimmer.ShimmerFrameLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoryItemActivity : BaseActivity() {

    private var currentPage = 0
    private val pageSize = 10

    private lateinit var shimmerLayout: ShimmerFrameLayout

    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private val recipeList = mutableListOf<RecipeResponse.Recipe>()
    private val allRecipes = mutableListOf<RecipeResponse.Recipe>()

    private val apiKey = BuildConfig.API_KEY


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setActivityLayout(R.layout.category_items)

        initializeBottomNavigation(setSelected = false)

        initializeBottomNavigation()
        setupBackButton()

        shimmerLayout = findViewById(R.id.shimmerLayout)

        recyclerView = findViewById(R.id.recipeRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recipeAdapter = RecipeAdapter(recipeList) { recipe ->
            val intent = Intent(this, FoodDetailsActivity::class.java)
            intent.putExtra("ID", recipe.id)
            startActivity(intent)
        }
        recyclerView.adapter = recipeAdapter

        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        searchEditText.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(word: Editable?) {
                val query = word.toString().lowercase().trim()
                filterRecipes(query)
            }

        })


        val previousPageBtn = findViewById<Button>(R.id.previousPage)
        val nextPageBtn = findViewById<Button>(R.id.nextPage)


        val category = intent.getStringExtra("CATEGORY") ?: ""
        val categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Smart Chef"
        val sort = intent.getStringExtra("POPULARITY") ?: ""

        val categoryHeading = findViewById<TextView>(R.id.category_show_heading)
        categoryHeading.text = categoryName

        setupBackButton()

        val apiService = RetrofitClient.getRetrofitInstance().create(ApiService::class.java)

        shimmerLayout.visibility = android.view.View.VISIBLE

        fetchRecipe(apiService, category, sort)

        previousPageBtn.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                fetchRecipe(apiService, category, sort)
            }
        }

        nextPageBtn.setOnClickListener {
            currentPage++
            fetchRecipe(apiService, category, sort)
        }
    }

    private fun fetchRecipe(apiService: ApiService, category: String, sort: String) {
        val offset = currentPage * pageSize
        val call: Call<RecipeResponse> = apiService.getRecipe(
            category,
            apiKey,
            offset,
            sort
        )
        call.enqueue(object : Callback<RecipeResponse> {
            override fun onResponse(
                call: Call<RecipeResponse>,
                response: Response<RecipeResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { recipeResponse ->

                        val recipes = recipeResponse.recipes
                        if (recipes.isNotEmpty()) {
                            allRecipes.clear()
                            allRecipes.addAll(recipes)
                            recipeList.clear()
                            recipeList.addAll(recipes)
                            recipeAdapter.notifyDataSetChanged()

                            updateButtonState(recipeResponse.totalResults)
                        } else {
                            Toast.makeText(
                                this@CategoryItemActivity,
                                "No recipes found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@CategoryItemActivity,
                        "API Error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                shimmerLayout.visibility = android.view.View.GONE
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                Toast.makeText(
                    this@CategoryItemActivity,
                    "API call failed: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()

                shimmerLayout.visibility = android.view.View.GONE

            }
        })
    }

    private fun filterRecipes(query: String) {
        val filteredList = if(query.isEmpty()){
            allRecipes
        }else{
            allRecipes.filter {
                it.title?.contains(query , ignoreCase = true) == true
            }
        }
        recipeAdapter.updateList(filteredList)
    }

    private fun updateButtonState(totalResults: Int) {
        val previousPageBtn = findViewById<Button>(R.id.previousPage)
        val nextPageBtn = findViewById<Button>(R.id.nextPage)

        val maxPages = (totalResults / pageSize)


        if (currentPage == 0) {
            previousPageBtn.isEnabled = false
            previousPageBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            previousPageBtn.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            previousPageBtn.isEnabled = true
            previousPageBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.red_500))
            previousPageBtn.setTextColor(ContextCompat.getColor(this, R.color.white))
        }


        if (currentPage >= maxPages) {
            nextPageBtn.isEnabled = false
            nextPageBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            nextPageBtn.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            nextPageBtn.isEnabled = true
            nextPageBtn.setBackgroundColor(ContextCompat.getColor(this, R.color.red_500))
            nextPageBtn.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
    }
}