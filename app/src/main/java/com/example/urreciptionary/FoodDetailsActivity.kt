package com.example.urreciptionary

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.example.urreciptionary.network.ApiService
import com.example.urreciptionary.network.NutritionWidgetResponse
import com.example.urreciptionary.network.RecipeDetailsResponse
import com.example.urreciptionary.network.RecipeWithDetails
import com.example.urreciptionary.network.RetrofitClient
import com.example.urreciptionary.utils.FavoriteManager
import com.example.urreciptionary.utils.ShowFoodInMealPlanUtils
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import pl.droidsonroids.gif.GifImageView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FoodDetailsActivity : BaseActivity() {
    private lateinit var btnFavorite: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setActivityLayout(R.layout.activity_food_details)
//        initializeBottomNavigation()
        setupBackButton()

        val backBtn = findViewById<ImageButton>(R.id.backButton)

        val expandCollapseIcon1 = findViewById<ImageView>(R.id.expandCollapseIcon)
        val instructionsHeader1 = findViewById<TextView>(R.id.instructionsHeader)
        val instructionsLayout1 = findViewById<LinearLayout>(R.id.instructionsExpandableLayout)

        val expandCollapseIcon2 = findViewById<ImageView>(R.id.expandCollapseIconTwo)
        val instructionsHeader2 = findViewById<TextView>(R.id.instructionsHeaderTwo)
        val instructionsLayout2 = findViewById<LinearLayout>(R.id.instructionsExpandableLayoutTwo)

        val expandCollapseIcon3 = findViewById<ImageView>(R.id.expandCollapseIconThree)
        val instructionsHeader3 = findViewById<TextView>(R.id.instructionsHeaderThree)
        val instructionsLayout3 = findViewById<LinearLayout>(R.id.instructionsExpandableLayoutThree)


        if (backBtn == null || expandCollapseIcon1 == null || instructionsHeader1 == null || instructionsLayout1 == null) {
            return
        }

        val id = intent.getIntExtra("ID", -1)

        btnFavorite = findViewById(R.id.btnFavorite)

        val buttonMealPlan = findViewById<ImageView>(R.id.btnMealPlan)
        buttonMealPlan.setOnClickListener {
            val recipeId = intent.getIntExtra("ID", -1)
            if(recipeId != -1){
                removeIncorrectRecipeIds(this)
                ShowFoodInMealPlanUtils.saveRecipeId(this,recipeId)
            }
        }

        if (id != -1) {
            fetchRecipeDetail(id)
            fetchNutritionInfo(id)
        } else {
            errorToast(this, "Recipe ID not found")
        }

        instructionsLayout1.visibility = View.VISIBLE
        expandCollapseIcon1.setImageResource(R.drawable.ic_minus)

        instructionsHeader1.setOnClickListener {
            toggleExpandCollapse(instructionsLayout1, expandCollapseIcon1)
        }

        expandCollapseIcon1.setOnClickListener {
            toggleExpandCollapse(instructionsLayout1, expandCollapseIcon1)
        }

        instructionsHeader2.setOnClickListener {
            toggleExpandCollapse(instructionsLayout2, expandCollapseIcon2)
        }

        expandCollapseIcon2.setOnClickListener {
            toggleExpandCollapse(instructionsLayout2, expandCollapseIcon2)
        }

        instructionsHeader3.setOnClickListener {
            toggleExpandCollapse(instructionsLayout3, expandCollapseIcon2)
        }

        expandCollapseIcon3.setOnClickListener {
            toggleExpandCollapse(instructionsLayout3, expandCollapseIcon3)
        }

    }

    private fun saveRecipeIdToPrefs(recipeId: Int) {
        val prefs = getSharedPreferences("MealPlans", Context.MODE_PRIVATE)
        val idSet = prefs.getStringSet("recipe_ids", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        idSet.add(recipeId.toString())
        prefs.edit().putStringSet("recipe_ids", idSet).apply()
    }

    private fun toggleExpandCollapse(
        instructionsLayout: LinearLayout,
        expandCollapseIcon: ImageView
    ) {
        if (instructionsLayout.visibility == View.GONE) {
            instructionsLayout.visibility = View.VISIBLE
            expandCollapseIcon.setImageResource(R.drawable.ic_minus)
        } else {
            instructionsLayout.visibility = View.GONE
            expandCollapseIcon.setImageResource(R.drawable.ic_plus)
        }
    }

    val api_key = BuildConfig.API_KEY

    private fun fetchRecipeDetail(id: Int) {
        val apiService = RetrofitClient.getRetrofitInstance().create(ApiService::class.java)
        val call: Call<RecipeDetailsResponse> =
            apiService.getRecipeDetails(id, api_key)

        call.enqueue(object : Callback<RecipeDetailsResponse> {

            override fun onResponse(
                call: Call<RecipeDetailsResponse>,
                response: Response<RecipeDetailsResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { recipeDetails ->

                        val recipeWithDetails = RecipeWithDetails(
                            recipeDetails.id,
                            recipeDetails.title,
                            recipeDetails.image,
                            recipeDetails.readyInMinutes,
                            recipeDetails.likes ?: 0
                        )

                        btnFavorite.setOnClickListener {
                            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                            if (currentUserId != null) {
                                FavoriteManager.toggleFavorite(this@FoodDetailsActivity, recipeWithDetails, currentUserId) { isFavorite ->
                                    if (isFavorite) {
                                        btnFavorite.setImageResource(R.drawable.ic_fav_filled)
                                    } else {
                                        btnFavorite.setImageResource(R.drawable.ic_favourite)
                                    }
                                }
                            } else {
                                errorToast(this@FoodDetailsActivity, "Please log in to use favorites")
                            }
                        }

                        Glide.with(this@FoodDetailsActivity).load(recipeDetails.image)
                            .into(findViewById(R.id.foodImage))

                        findViewById<TextView>(R.id.foodTitle).text = recipeDetails.title

                        findViewById<TextView>(R.id.prepTime).text =
                            "${recipeDetails.readyInMinutes} Min"

                        findViewById<TextView>(R.id.vegType).text =
                            if (recipeDetails.isVegetarian) "Veg" else "Non-Veg"

                        val rawInstruction =
                            recipeDetails.instructions ?: "No instructions available"
                        val removeUnnecessary = rawInstruction
                            .replace("<ol>", "")
                            .replace("</ol>", "")
                            .replace("<p>", "")
                            .replace("</p>", "")
                            .replace("<span>", "")
                            .replace("</span>", "")

                        val customize = removeUnnecessary.split("<li>")
                            .filter { it.isNotBlank() }
                            .mapIndexed { index, s ->
                                "${index + 1}. ${
                                    s.replace("</li>", "").trim()
                                }"
                            }

                        val properFormat = customize.joinToString("\n\n")
                        findViewById<TextView>(R.id.instructionsText).text = properFormat

                        val instructionsLayout =
                            findViewById<LinearLayout>(R.id.instructionsExpandableLayout)
                        val typefaces = ResourcesCompat.getFont(
                            this@FoodDetailsActivity,
                            R.font.bricolage_regular
                        )

                        recipeDetails.analyzedInstructions?.forEach { analyzeInstruction ->
                            analyzeInstruction.steps?.forEachIndexed { index, step ->
                                val stepTextView = TextView(this@FoodDetailsActivity).apply {
                                    val formattedStepString =
                                        getString(R.string.step_text, step.number, step.step)
                                    text = formattedStepString
                                    textSize = 16f
                                    typeface = typefaces
                                    setTextColor(Color.BLACK)
                                }
                                removeViewFromParent(stepTextView)
                                instructionsLayout.addView(stepTextView)

                                val divider = View(this@FoodDetailsActivity).apply {
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        1
                                    ).apply {
                                        topMargin = (10 * resources.displayMetrics.density).toInt()
                                        bottomMargin =
                                            (10 * resources.displayMetrics.density).toInt()
                                    }
                                    setBackgroundResource(R.drawable.divider_horizontal)
                                }

                                removeViewFromParent(divider)
                                instructionsLayout.addView(divider)

                                val ingredientTextView = TextView(this@FoodDetailsActivity).apply {
                                    val ingredientsName =
                                        step.ingredients?.joinToString(", ") { it.name }

                                    if (!ingredientsName.isNullOrEmpty()) {
                                        text = getString(R.string.ingredients_text, ingredientsName)
                                        textSize = 16f
                                        typeface = typefaces
                                        setTextColor(Color.BLACK)
                                    } else {
                                        visibility = View.GONE
                                    }
                                }

                                removeViewFromParent(divider)
                                instructionsLayout.addView(ingredientTextView)

                                removeViewFromParent(divider)
                                instructionsLayout.addView(divider)

                            }
                        }

//                        Ingredients
                        val ingredientLayout = findViewById<LinearLayout>(R.id.ingredientLayout)
                        ingredientLayout.removeAllViews()

                        recipeDetails.ingredients.forEach { ingredient ->
                            val ingredientItemLayout = LayoutInflater.from(this@FoodDetailsActivity)
                                .inflate(R.layout.ingredient_item, null)

                            val ingredientName =
                                ingredientItemLayout.findViewById<TextView>(R.id.ingredient_name)
                            val ingredientAmount =
                                ingredientItemLayout.findViewById<TextView>(R.id.ingredient_amount)

                            ingredientName.text = ingredient.name
                            ingredientAmount.text = "${ingredient.amount} ${ingredient.unit}"

                            ingredientLayout.addView(ingredientItemLayout)
                        }

//                        Diet
                        val dietLayout = findViewById<LinearLayout>(R.id.dietLayout)
                        dietLayout.removeAllViews()
                        recipeDetails.diets.forEach { diet ->
                            val dietLabel = TextView(this@FoodDetailsActivity)

                            val typeface = ResourcesCompat.getFont(
                                this@FoodDetailsActivity,
                                R.font.bricolage_medium
                            )
                            dietLabel.typeface = typeface

                            dietLabel.text = diet
                            dietLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                            dietLabel.setTextColor(Color.BLACK)
                            dietLabel.setPadding(15, 8, 15, 8)

                            val drawable = GradientDrawable().apply {
                                cornerRadius = 20f
                                setStroke(2, Color.parseColor("#FF0000"))
                            }


                            dietLabel.background = drawable

                            dietLayout.addView(dietLabel)

                            val layoutParams = dietLabel.layoutParams as LinearLayout.LayoutParams
                            layoutParams.setMargins(0, 0, 8, 0)
                            dietLabel.layoutParams = layoutParams
                        }

//                        Occasion
                        val occasionLayout = findViewById<LinearLayout>(R.id.occasionLayout)
                        val occasionRecycler = findViewById<RecyclerView>(R.id.occasionRecycler)
                        val gridLayoutManager = GridLayoutManager(this@FoodDetailsActivity, 3)
                        occasionRecycler.layoutManager = gridLayoutManager
                        if (recipeDetails.occasions.isNotEmpty()) {
                            occasionLayout.visibility = View.VISIBLE
                            val adapter = OccasionAdapter(recipeDetails.occasions)
                            occasionRecycler.adapter = adapter
                        } else {
                            occasionLayout.visibility = View.GONE
                        }
                    }
                } else {
                    errorToast(
                        this@FoodDetailsActivity,
                        "Error fetching recipe details")
                }
            }

            override fun onFailure(call: Call<RecipeDetailsResponse>, t: Throwable) {
                errorToast(
                    this@FoodDetailsActivity,
                    "API call failed: ${t.message}")
            }
        })
    }

    private fun getIconFromResource(occasion: String?): Int {
        return when (occasion?.lowercase()) {
            "christmas" -> R.drawable.ic_occasion_christmas
            "easter" -> R.drawable.ic_occasion_easter
            "fall" -> R.drawable.ic_occasion_fall
            "thanksgiving" -> R.drawable.ic_occasion_thives
            "halloween" -> R.drawable.ic_occasion_halloween
            "new year’s eve" -> R.drawable.ic_occasion_new_year
            "valentine’s day" -> R.drawable.ic_occasion_valentine
            "hanukkah" -> R.drawable.ic_occasion_hanukkah
            "mother’s day" -> R.drawable.ic_occasion_mothers_day
            "father’s day" -> R.drawable.ic_occasion_fathers_day
            "st. patrick’s day" -> R.drawable.ic_occasion_patrics
            "summer" -> R.drawable.ic_occasion_summer
            "winter" -> R.drawable.ic_occasion_winter
            "spring" -> R.drawable.ic_occasion_spring
            else -> R.drawable.ic_occasion_default
        }
    }

    private fun removeViewFromParent(view: View) {
        val parent = view.parent
        if (parent != null && parent is ViewGroup) {
            parent.removeView(view)
        }
    }

    private fun fetchNutritionInfo(id: Int) {
        val api_key = BuildConfig.API_KEY
        val apiService = RetrofitClient.getRetrofitInstance().create(ApiService::class.java)
        val call: Call<NutritionWidgetResponse> =
            apiService.getNutritionWidget(id, api_key)

        call.enqueue(object : Callback<NutritionWidgetResponse> {
            override fun onResponse(
                call: Call<NutritionWidgetResponse>,
                response: Response<NutritionWidgetResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { ntResponse ->
                        findViewById<TextView>(R.id.calories).text = "${ntResponse.calories} Kcal"
                        findViewById<TextView>(R.id.carbsValue).text = ntResponse.carbs
                        findViewById<TextView>(R.id.proteinValue).text = ntResponse.protein
                        findViewById<TextView>(R.id.fatValue).text = ntResponse.fat
                        val fiberNutrient =
                            ntResponse.nutrients.find { nutrient -> nutrient.name == "Fiber" }
                        findViewById<TextView>(R.id.fiberValue).text = getString(
                            R.string.nutrient_value,
                            fiberNutrient?.amount ?: "NA",
                            fiberNutrient?.unit ?: ""
                        )
                        val sugarNutrient =
                            ntResponse.nutrients.find { nutrient -> nutrient.name == "Sugar" }
                        findViewById<TextView>(R.id.sugarValue).text = getString(
                            R.string.nutrient_value,
                            sugarNutrient?.amount ?: "NA",
                            sugarNutrient?.unit ?: ""
                        )
                        val sodiumNutrient =
                            ntResponse.nutrients.find { nutrient -> nutrient.name == "Sodium" }
                        findViewById<TextView>(R.id.sodiumValue).text = getString(
                            R.string.nutrient_value,
                            sodiumNutrient?.amount ?: "NA",
                            sodiumNutrient?.unit ?: ""
                        )
                        val chlNutrient =
                            ntResponse.nutrients.find { nutrient -> nutrient.name == "Cholesterol" }
                        findViewById<TextView>(R.id.cholesterolValue).text = getString(
                            R.string.nutrient_value,
                            chlNutrient?.amount ?: "NA",
                            chlNutrient?.unit ?: ""
                        )
                    }
                } else {
                    errorToast(
                        this@FoodDetailsActivity,
                        "Error fetching nutrition info")
                }
            }

            override fun onFailure(call: Call<NutritionWidgetResponse>, t: Throwable) {
                errorToast(
                    this@FoodDetailsActivity,
                    "API call failed: ${t.message}")
            }

        })
    }

    fun removeIncorrectRecipeIds(context: Context) {
        val prefs = context.getSharedPreferences("MealPlans", Context.MODE_PRIVATE)
        prefs.edit().remove("recipe_ids").apply()
    }


}