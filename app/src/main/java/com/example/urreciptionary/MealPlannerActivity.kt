package com.example.urreciptionary

import android.content.Context
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.urreciptionary.network.ApiService
import com.example.urreciptionary.network.Meal
import com.example.urreciptionary.network.NutritionWidgetResponse
import com.example.urreciptionary.network.RecipeDetailsResponse
import com.example.urreciptionary.network.RecipeResponse
import com.example.urreciptionary.network.RetrofitClient
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale


class MealPlannerActivity : BaseActivity() {

    private var selectedDay: View? = null
    private var selectedCard: MaterialCardView? = null
    private var selectedDate: String = ""
    private lateinit var note: EditText

    private var selectedMeals = mutableMapOf<String, MutableList<Meal>>()

    private lateinit var cardBreakfast: LinearLayout
    private lateinit var linearLayoutContent: LinearLayout
    private lateinit var ivBreakfastExpand: ImageView
    private lateinit var ivBreakfast: ImageView
    private lateinit var tvBreakfast: TextView

    private lateinit var cardLunch: LinearLayout
    private lateinit var lunchLinearLayoutContent: LinearLayout
    private lateinit var ivLunchExpand: ImageView
    private lateinit var ivLunch: ImageView
    private lateinit var tvLunch: TextView

    private lateinit var cardDinner: LinearLayout
    private lateinit var linearLayoutContentDinner: LinearLayout
    private lateinit var ivDinnerExpand: ImageView
    private lateinit var ivDinner: ImageView
    private lateinit var tvDinner: TextView

//    private lateinit var otherCardView: CardView
    private lateinit var cardOther: LinearLayout
    private lateinit var layoutContainer: LinearLayout
    private lateinit var ivOtherExpand: ImageView
    private lateinit var ivOther: ImageView
    private lateinit var tvOther: TextView

    private var isExpanded = false
    private var isBreakfastExpanded = false
    private var isLunchExpanded = false
    private var isDinnerExpanded = false
    private var isOtherExpanded = false

    private lateinit var shimmerLayoutBreakfast: ShimmerFrameLayout
    private lateinit var shimmerLayoutLunch: ShimmerFrameLayout
    private lateinit var shimmerLayoutDinner: ShimmerFrameLayout
    private lateinit var shimmerLayoutOther: ShimmerFrameLayout

    private val loadedRecipes = mutableMapOf<String, Boolean>()
    private var currentPage = 1
    private val pageSize = 2

    private var dietParam = ""

    private val apiKey = BuildConfig.API_KEY

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setActivityLayout(R.layout.activity_meal_planner)

        initializeBottomNavigation()
        setupBackButton()

        isBreakfastExpanded = true
        initializeViews()
        updateVisibility()

        note = findViewById<EditText?>(R.id.editTextNote)

        val chipGroupDiet: ChipGroup = findViewById(R.id.chipGroupDiet)

        val selectedDiet = mutableListOf<String>()
        chipGroupDiet.setOnCheckedStateChangeListener { group, checkId ->
            selectedDiet.clear()
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as Chip
                if (chip.isChecked) {
                    selectedDiet.add(chip.text.toString())
                }
            }
            dietParam = selectedDiet.joinToString(",")
            fetchRecipesForAllCategories()
        }


//        Time date
        val currentDate = LocalDate.now()
        val currentMonth = currentDate.month
        val currentYear = currentDate.year
        selectedDate = currentDate.toString()

        val firstDayOfMonth = LocalDate.of(currentYear, currentMonth, 1)
        val daysInMonth = firstDayOfMonth.lengthOfMonth()
        val monthName = currentMonth.getDisplayName(TextStyle.FULL, Locale.getDefault())

        val layoutCalendar = findViewById<LinearLayout>(R.id.layoutCalendar)

        val tvMonth = findViewById<TextView>(R.id.tvMonth)
        tvMonth.text = "$monthName $currentYear"

        layoutCalendar.removeAllViews()

        for (day in 1..daysInMonth) {
            val dayDate = LocalDate.of(currentYear, currentMonth, day)
            if (dayDate.isBefore(currentDate)) continue
            val dayContainer = LayoutInflater.from(this)
                .inflate(R.layout.day_container_layout, null) as ConstraintLayout

            val dayName = dayDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val dayNameTextView = dayContainer.findViewById<TextView>(R.id.dayNameTextView)
            dayNameTextView.text = dayName

            val dayTextView = dayContainer.findViewById<TextView>(R.id.dayTextView)
            dayTextView.text = day.toString()

            val dayFrame = dayContainer.findViewById<FrameLayout>(R.id.dayFrame)

            dayFrame.setOnClickListener {
                selectedDay?.findViewById<FrameLayout>(R.id.dayFrame)
                    ?.setBackgroundResource(R.drawable.bg_calendar_day_default)
                dayFrame.setBackgroundResource(R.drawable.bg_calendar_day_selected)
                selectedDay = dayContainer

                selectedDate = dayDate.toString()

                val springScaleX = SpringAnimation(dayContainer, SpringAnimation.SCALE_X).apply {
                    spring = SpringForce(1f).apply {
                        dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                        stiffness = SpringForce.STIFFNESS_LOW
                    }
                }
                springScaleX.setStartValue(1.4f)

                val springScaleY = SpringAnimation(dayContainer, SpringAnimation.SCALE_Y).apply {
                    spring = SpringForce(1f).apply {
                        dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                        stiffness = SpringForce.STIFFNESS_LOW
                    }
                }
                springScaleY.setStartValue(1.4f)

                springScaleX.start()
                springScaleY.start()

            }

            if (dayDate.isEqual(currentDate)) {
                dayFrame.setBackgroundResource(R.drawable.bg_calendar_day_selected)
                selectedDay = dayContainer
            }

            layoutCalendar.addView(dayContainer)
        }

        val apiService = RetrofitClient.getRetrofitInstance().create(ApiService::class.java)

        cardBreakfast = findViewById(R.id.headingItem)
        shimmerLayoutBreakfast = findViewById(R.id.shimmer_layout_breakfast)
        linearLayoutContent = findViewById(R.id.linearLayoutContent)
        ivBreakfastExpand = findViewById(R.id.ivBreakfastExpand)
        ivBreakfast = findViewById(R.id.ivBreakfast)
        tvBreakfast = findViewById(R.id.tvBreakfast)
        linearLayoutContent.visibility = View.GONE

        if (loadedRecipes["Breakfast"] != true) {
            shimmerLayoutBreakfast.visibility = View.VISIBLE
            clearRecipes("Breakfast")
            fetchRecipe(apiService, category = "Breakfast", sort = "time", dietParam = dietParam) {
                shimmerLayoutBreakfast.visibility = View.GONE
                loadedRecipes["Breakfast"] = true
                linearLayoutContent.visibility = View.VISIBLE
            }
        }

        cardLunch = findViewById(R.id.headingItemLunch)
        lunchLinearLayoutContent = findViewById(R.id.linearLayoutContentLunch)
        shimmerLayoutLunch = findViewById(R.id.shimmer_layout_lunch)
        ivLunchExpand = findViewById(R.id.ivLunchExpand)
        ivLunch = findViewById(R.id.ivLunch)
        tvLunch = findViewById(R.id.tvLunch)
        lunchLinearLayoutContent.visibility = View.GONE

        cardDinner = findViewById(R.id.headingItemDinner)
        linearLayoutContentDinner = findViewById(R.id.linearLayoutContentDinner)
        ivDinnerExpand = findViewById(R.id.ivDinnerExpand)
        shimmerLayoutDinner = findViewById(R.id.shimmer_layout_dinner)
        ivDinner = findViewById(R.id.ivDinner)
        tvDinner = findViewById(R.id.tvDinner)
        linearLayoutContentDinner.visibility = View.GONE

//        otherCardView = findViewById(R.id.cardOther)
        cardOther = findViewById(R.id.headingItemOther)
        layoutContainer = findViewById(R.id.linearLayoutContentOther)
        ivOtherExpand = findViewById(R.id.ivOtherExpand)
        shimmerLayoutOther = findViewById(R.id.shimmer_layout_other)
        ivOther = findViewById(R.id.ivOther)
        tvOther = findViewById(R.id.tvOther)
        layoutContainer.visibility = View.GONE

        cardBreakfast.setOnClickListener {
            isBreakfastExpanded = !isBreakfastExpanded
            updateVisibility()

            if (isBreakfastExpanded && loadedRecipes["Breakfast"] != true) {
                shimmerLayoutBreakfast.visibility = View.VISIBLE
                clearRecipes("Breakfast")
                fetchRecipe(
                    apiService,
                    category = "Breakfast",
                    sort = "time",
                    dietParam = dietParam
                ) {
                    shimmerLayoutBreakfast.visibility = View.GONE
                    loadedRecipes["Breakfast"] = true
                }
            }
        }

        cardLunch.setOnClickListener {
            isLunchExpanded = !isLunchExpanded
            updateVisibility()

            if (isLunchExpanded && loadedRecipes["Lunch"] != true) {
                shimmerLayoutLunch.visibility = View.VISIBLE
                clearRecipes("Lunch")
                fetchRecipe(apiService, category = "Lunch", sort = "time", dietParam = dietParam) {
                    shimmerLayoutLunch.visibility = View.GONE
                    loadedRecipes["Lunch"] = true
                }
            }
        }

        cardDinner.setOnClickListener {
            isDinnerExpanded = !isDinnerExpanded
            updateVisibility()

            if (isDinnerExpanded && loadedRecipes["Dinner"] != true) {
                shimmerLayoutDinner.visibility = View.VISIBLE
                clearRecipes("Dinner")
                fetchRecipe(apiService, category = "Dinner", sort = "time", dietParam = dietParam) {
                    shimmerLayoutDinner.visibility = View.GONE
                    loadedRecipes["Dinner"] = true
                }
            }
        }

        cardOther.setOnClickListener {
            isOtherExpanded = !isOtherExpanded
            updateVisibility()

            if (isOtherExpanded) {
                fetchSavedMealPlanRecipes()
            }
        }

        val btnSaveMealPlan = findViewById<Button>(R.id.btnSaveMealPlan)
        btnSaveMealPlan.setOnClickListener {
            saveFullMealPlanToFirebase()
        }
    }

    private fun fetchRecipesForAllCategories() {
        val apiService = RetrofitClient.getRetrofitInstance().create(ApiService::class.java)

        clearRecipes("Breakfast")
        clearRecipes("Lunch")
        clearRecipes("Dinner")

        fetchRecipe(apiService, category = "Breakfast", sort = "time", dietParam = dietParam) {
            shimmerLayoutBreakfast.visibility = View.GONE
            loadedRecipes["Breakfast"] = true
            linearLayoutContent.visibility = View.VISIBLE
        }

        fetchRecipe(apiService, category = "Lunch", sort = "time", dietParam = dietParam) {
            shimmerLayoutLunch.visibility = View.GONE
            loadedRecipes["Lunch"] = true
            lunchLinearLayoutContent.visibility = View.VISIBLE
        }

        fetchRecipe(apiService, category = "Dinner", sort = "time", dietParam = dietParam) {
            shimmerLayoutDinner.visibility = View.GONE
            loadedRecipes["Dinner"] = true
            linearLayoutContentDinner.visibility = View.VISIBLE
        }
    }

    private fun clearRecipes(category: String) {
        when (category) {
            "Breakfast" -> linearLayoutContent.removeAllViews()
            "Lunch" -> lunchLinearLayoutContent.removeAllViews()
            "Dinner" -> linearLayoutContentDinner.removeAllViews()
        }
    }

    private fun updateVisibility() {
        linearLayoutContent.visibility = if (isBreakfastExpanded) View.VISIBLE else View.GONE
        ivBreakfastExpand.setImageResource(if (isBreakfastExpanded) R.drawable.ic_minus else R.drawable.ic_plus)

        lunchLinearLayoutContent.visibility = if (isLunchExpanded) View.VISIBLE else View.GONE
        ivLunchExpand.setImageResource(if (isLunchExpanded) R.drawable.ic_minus else R.drawable.ic_plus)

        linearLayoutContentDinner.visibility = if (isDinnerExpanded) View.VISIBLE else View.GONE
        ivDinnerExpand.setImageResource(if (isDinnerExpanded) R.drawable.ic_minus else R.drawable.ic_plus)

        layoutContainer.visibility = if (isOtherExpanded) View.VISIBLE else View.GONE
        ivOtherExpand.setImageResource(if (isOtherExpanded) R.drawable.ic_minus else R.drawable.ic_plus)

        shimmerLayoutBreakfast.visibility =
            if (isBreakfastExpanded && !loadedRecipes.containsKey("Breakfast")) View.VISIBLE else View.GONE
        shimmerLayoutLunch.visibility =
            if (isLunchExpanded && !loadedRecipes.containsKey("Lunch")) View.VISIBLE else View.GONE
        shimmerLayoutDinner.visibility =
            if (isDinnerExpanded && !loadedRecipes.containsKey("Dinner")) View.VISIBLE else View.GONE
    }

    private fun initializeViews() {
        cardBreakfast = findViewById(R.id.headingItem)
        shimmerLayoutBreakfast = findViewById(R.id.shimmer_layout_breakfast)
        linearLayoutContent = findViewById(R.id.linearLayoutContent)
        ivBreakfastExpand = findViewById(R.id.ivBreakfastExpand)
        ivBreakfast = findViewById(R.id.ivBreakfast)
        tvBreakfast = findViewById(R.id.tvBreakfast)
        linearLayoutContent.visibility = View.GONE

        cardLunch = findViewById(R.id.headingItemLunch)
        lunchLinearLayoutContent = findViewById(R.id.linearLayoutContentLunch)
        shimmerLayoutLunch = findViewById(R.id.shimmer_layout_lunch)
        ivLunchExpand = findViewById(R.id.ivLunchExpand)
        ivLunch = findViewById(R.id.ivLunch)
        tvLunch = findViewById(R.id.tvLunch)
        lunchLinearLayoutContent.visibility = View.GONE

        cardDinner = findViewById(R.id.headingItemDinner)
        linearLayoutContentDinner = findViewById(R.id.linearLayoutContentDinner)
        ivDinnerExpand = findViewById(R.id.ivDinnerExpand)
        shimmerLayoutDinner = findViewById(R.id.shimmer_layout_dinner)
        ivDinner = findViewById(R.id.ivDinner)
        tvDinner = findViewById(R.id.tvDinner)
        linearLayoutContentDinner.visibility = View.GONE

        cardOther = findViewById(R.id.headingItemOther)
        layoutContainer = findViewById(R.id.linearLayoutContentOther)
        ivOtherExpand = findViewById(R.id.ivOtherExpand)
        shimmerLayoutOther = findViewById(R.id.shimmer_layout_other)
        ivOther = findViewById(R.id.ivOther)
        tvOther = findViewById(R.id.tvOther)
        layoutContainer.visibility = View.GONE

    }

    private fun fetchSavedMealPlanRecipes() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null){
            val userId = currentUser.uid
            val prefs = getSharedPreferences("MealPlans", Context.MODE_PRIVATE)
            val savedIds = prefs.getStringSet(userId, emptySet()) ?: emptySet()
            if (savedIds.isEmpty()) {
//                otherCardView.visibility = View.GONE
                warningToast(this, "No saved meals found")
                return
            }

//            otherCardView.visibility = View.VISIBLE

            val apiService = RetrofitClient.getRetrofitInstance().create(ApiService::class.java)
            layoutContainer = findViewById(R.id.linearLayoutContentOther)

            layoutContainer.removeAllViews()
            for (idStr in savedIds) {
                val id = idStr.toIntOrNull() ?: continue

                apiService.getRecipeDetails(id, BuildConfig.API_KEY)
                    .enqueue(object : Callback<RecipeDetailsResponse> {
                        override fun onResponse(
                            call: Call<RecipeDetailsResponse>,
                            response: Response<RecipeDetailsResponse>
                        ) {
                            if (response.isSuccessful) {
                                response.body()?.let { recipeDetails ->
                                    val cardView = LayoutInflater.from(this@MealPlannerActivity)
                                        .inflate(R.layout.meal_planner_cardview_recipe_item, layoutContainer, false)

                                    val plusIcon: ImageButton = cardView.findViewById(R.id.plus_icon)
                                    val recipeCard: MaterialCardView =
                                        cardView.findViewById(R.id.recipe_card)
                                    recipeCard.setCardBackgroundColor(
                                        ContextCompat.getColor(
                                            this@MealPlannerActivity,
                                            R.color.white
                                        )
                                    )
                                    var isSelected = false
                                    var completed = false

                                    plusIcon.setOnClickListener {
                                        isSelected = !isSelected

                                        if (isSelected) {
                                            recipeCard.strokeColor = ContextCompat.getColor(
                                                this@MealPlannerActivity,
                                                R.color.green
                                            )
                                            recipeCard.cardElevation = 8f
                                            plusIcon.setImageResource(R.drawable.ic_minus)
                                            recipeCard.setCardBackgroundColor(
                                                ContextCompat.getColor(
                                                    this@MealPlannerActivity,
                                                    R.color.light_red_background
                                                )
                                            )

                                            fetchRecipesById(recipeDetails.id) { readyInMinutes, healthScore ->

                                                val nutritionCall = apiService.getNutritionWidget(recipeDetails.id, apiKey)

                                                nutritionCall.enqueue(object: Callback<NutritionWidgetResponse>{
                                                    override fun onResponse(
                                                        call: Call<NutritionWidgetResponse>,
                                                        response: Response<NutritionWidgetResponse>
                                                    ) {
                                                        if (response.isSuccessful) {
                                                            val nt = response.body()

                                                            val kcal = nt?.calories?.replace("kcal", "", true)?.trim()?.toDoubleOrNull() ?: 0.0
                                                            val carbs = nt?.carbs?.replace("g", "", true)?.trim()?.toDoubleOrNull() ?: 0.0
                                                            val fat = nt?.fat?.replace("g", "", true)?.trim()?.toDoubleOrNull() ?: 0.0
                                                            val protein = nt?.protein?.replace("g", "", true)?.trim()?.toDoubleOrNull() ?: 0.0

                                                            val mealData = Meal(
                                                                recipeDetails.id,
                                                                recipeDetails.title,
                                                                recipeDetails.image ?: "",
                                                                readyInMinutes,
                                                                healthScore,
                                                                note.text.toString() ?: "User hasn't added any comments 🤔",
                                                                kcal,
                                                                carbs,
                                                                fat,
                                                                protein,
                                                                isSelected,
                                                                completed
                                                            )

                                                            val mealList = selectedMeals.getOrPut("Breakfast") { mutableListOf() }

                                                            if (!mealList.any { it.id == recipeDetails.id }) {
                                                                mealList.add(mealData)
                                                            }
                                                        }else{
                                                            errorToast(this@MealPlannerActivity, "Failed: ${response.code()}")
                                                        }
                                                    }

                                                    override fun onFailure(
                                                        call: Call<NutritionWidgetResponse>,
                                                        t: Throwable
                                                    ) {
                                                        TODO("Not yet implemented")
                                                    }

                                                })

                                            }

                                        } else {
                                            recipeCard.strokeColor = ContextCompat.getColor(
                                                this@MealPlannerActivity,
                                                R.color.gray_light
                                            )
                                            recipeCard.cardElevation = 4f
                                            recipeCard.setCardBackgroundColor(
                                                ContextCompat.getColor(
                                                    this@MealPlannerActivity,
                                                    R.color.white
                                                )
                                            )
                                            plusIcon.setImageResource(R.drawable.ic_plus)
                                        }
                                    }

                                    val recipeTitle = cardView.findViewById<TextView>(R.id.recipe_title)
                                    val recipeImage = cardView.findViewById<ImageView>(R.id.recipe_image)
                                    val recipeTime = cardView.findViewById<TextView>(R.id.recipe_time)
                                    val recipeHealth = cardView.findViewById<TextView>(R.id.recipe_health)

                                    recipeTitle.text = recipeDetails.title
                                    recipeTime.text = "${recipeDetails.readyInMinutes} min"
                                    recipeHealth.text = "Health: ${recipeDetails.healthScore}"

                                    Glide.with(this@MealPlannerActivity)
                                        .load(recipeDetails.image)
                                        .placeholder(R.drawable.img_shimmer_background)
                                        .into(recipeImage)

                                    val showDetailsBtn: TextView =
                                        cardView.findViewById(R.id.showDetailsBtn)

                                    showDetailsBtn.setOnClickListener {
                                        val intent = Intent(
                                            this@MealPlannerActivity,
                                            FoodDetailsActivity::class.java
                                        )
                                        intent.putExtra("ID", recipeDetails.id)
                                        startActivity(intent)
                                    }
                                    layoutContainer.addView(cardView)
                                }
                            } else {
                                errorToast(this@MealPlannerActivity, "Error loading recipe ID $id")
                            }
                        }

                        override fun onFailure(call: Call<RecipeDetailsResponse>, t: Throwable) {
                            errorToast(this@MealPlannerActivity,"Something Went Wrong")
                        }
                    })
            }
        }
    }


    private fun fetchRecipe(
        apiService: ApiService,
        category: String,
        sort: String,
        dietParam: String,
        callback: () -> Unit
    ) {
        val offset = currentPage * pageSize

        val call: Call<RecipeResponse> =
            apiService.getMealPlannerRecipe(category, apiKey, offset, sort, dietParam)
        call.enqueue(object : Callback<RecipeResponse> {
            override fun onResponse(
                call: Call<RecipeResponse>,
                response: Response<RecipeResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { recipeResponse ->
                        val recipes = recipeResponse.recipes
                        if (recipes.isNotEmpty()) {

                            for (rc in recipes) {
                                val cardView = LayoutInflater.from(this@MealPlannerActivity)

                                    .inflate(
                                        R.layout.meal_planner_cardview_recipe_item,
                                        when (category) {
                                            "Breakfast" -> linearLayoutContent
                                            "Lunch" -> lunchLinearLayoutContent
                                            "Dinner" -> linearLayoutContentDinner
                                            else -> linearLayoutContent
                                        },
                                        false
                                    ) as ConstraintLayout

                                val plusIcon: ImageButton = cardView.findViewById(R.id.plus_icon)
                                val recipeCard: MaterialCardView =
                                    cardView.findViewById(R.id.recipe_card)
                                recipeCard.setCardBackgroundColor(
                                    ContextCompat.getColor(
                                        this@MealPlannerActivity,
                                        R.color.white
                                    )
                                )
                                var isSelected = false
                                var completed = false

                                plusIcon.setOnClickListener {
                                    isSelected = !isSelected

                                    if (isSelected) {
                                        recipeCard.strokeColor = ContextCompat.getColor(
                                            this@MealPlannerActivity,
                                            R.color.green
                                        )
                                        recipeCard.cardElevation = 8f
                                        plusIcon.setImageResource(R.drawable.ic_minus)
                                        recipeCard.setCardBackgroundColor(
                                            ContextCompat.getColor(
                                                this@MealPlannerActivity,
                                                R.color.light_red_background
                                            )
                                        )

                                        fetchRecipesById(rc.id) { readyInMinutes, healthScore ->

                                            val nutritionCall = apiService.getNutritionWidget(rc.id, apiKey)

                                            nutritionCall.enqueue(object: Callback<NutritionWidgetResponse>{
                                                override fun onResponse(
                                                    call: Call<NutritionWidgetResponse>,
                                                    response: Response<NutritionWidgetResponse>
                                                ) {
                                                    if (response.isSuccessful) {
                                                        val nt = response.body()

                                                        val kcal = nt?.calories?.replace("kcal", "", true)?.trim()?.toDoubleOrNull() ?: 0.0
                                                        val carbs = nt?.carbs?.replace("g", "", true)?.trim()?.toDoubleOrNull() ?: 0.0
                                                        val fat = nt?.fat?.replace("g", "", true)?.trim()?.toDoubleOrNull() ?: 0.0
                                                        val protein = nt?.protein?.replace("g", "", true)?.trim()?.toDoubleOrNull() ?: 0.0

                                                        val mealData = Meal(
                                                            rc.id,
                                                            rc.title,
                                                            rc.image ?: "",
                                                            readyInMinutes,
                                                            healthScore,
                                                            note.text.toString() ?: "User hasn't added any comments 🤔",
                                                            kcal,
                                                            carbs,
                                                            fat,
                                                            protein,
                                                            isSelected,
                                                            completed
                                                        )

                                                        val mealList = selectedMeals.getOrPut(category.lowercase()) { mutableListOf() }

                                                        if (!mealList.any { it.id == rc.id }) {
                                                            mealList.add(mealData)
                                                        }
                                                    }else{
                                                        errorToast(this@MealPlannerActivity, "Failed: ${response.code()}")
                                                    }
                                                }

                                                override fun onFailure(
                                                    call: Call<NutritionWidgetResponse>,
                                                    t: Throwable
                                                ) {
                                                    TODO("Not yet implemented")
                                                }

                                            })

                                        }

                                    } else {
                                        recipeCard.strokeColor = ContextCompat.getColor(
                                            this@MealPlannerActivity,
                                            R.color.gray_light
                                        )
                                        recipeCard.cardElevation = 4f
                                        recipeCard.setCardBackgroundColor(
                                            ContextCompat.getColor(
                                                this@MealPlannerActivity,
                                                R.color.white
                                            )
                                        )
                                        plusIcon.setImageResource(R.drawable.ic_plus)

                                        selectedMeals[category.lowercase()]?.removeIf { it.id == rc.id }
                                    }
                                }

                                val recipeImage: ImageView =
                                    cardView.findViewById(R.id.recipe_image)
                                val recipeTitle: TextView = cardView.findViewById(R.id.recipe_title)
                                val timeText: TextView = cardView.findViewById(R.id.recipe_time)
                                val healthScoreText: TextView =
                                    cardView.findViewById(R.id.recipe_health)

                                recipeTitle.text = rc.title

                                fetchRecipesById(rc.id) { readyInMinutes, healthScore ->
                                    val timeFormatted =
                                        getString(R.string.recipe_time_format, readyInMinutes)
                                    val healthScoreFormatted =
                                        getString(R.string.recipe_health_score_format, healthScore)

                                    timeText.text = timeFormatted
                                    healthScoreText.text = healthScoreFormatted
                                }

                                Glide.with(this@MealPlannerActivity)
                                    .load(rc.image)
                                    .placeholder(R.drawable.img_shimmer_background)
                                    .centerCrop()
                                    .override(200, 200)
                                    .transition(DrawableTransitionOptions.withCrossFade())
                                    .into(recipeImage)

                                val showDetailsBtn: TextView =
                                    cardView.findViewById(R.id.showDetailsBtn)

                                showDetailsBtn.setOnClickListener {
                                    val intent = Intent(
                                        this@MealPlannerActivity,
                                        FoodDetailsActivity::class.java
                                    )
                                    intent.putExtra("ID", rc.id)
                                    startActivity(intent)
                                }
                                when (category) {
                                    "Breakfast" -> linearLayoutContent.addView(cardView)
                                    "Lunch" -> lunchLinearLayoutContent.addView(cardView)
                                    "Dinner" -> linearLayoutContentDinner.addView(cardView)
                                }
                            }
                        } else {
                            errorToast(
                                this@MealPlannerActivity,
                                "No recipes found")
                        }
                    }
                } else {
                    errorToast(
                        this@MealPlannerActivity,
                        "API Error: ${response.code()}")
                }
                callback()
            }

            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                callback()
                errorToast(
                    this@MealPlannerActivity,
                    "API call failed: ${t.message}")

            }
        })
    }

    private fun fetchRecipesById(recipeId: Int, callback: (Int, Double) -> Unit) {

        val apiService = RetrofitClient.getRetrofitInstance().create(ApiService::class.java)
        val apiKey = BuildConfig.API_KEY

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


                            callback(readyInMinutes, healthScore)
                        }
                    } else {
                        errorToast(
                            this@MealPlannerActivity,
                            "Failed to fetch recipe details")
                    }
                }

                override fun onFailure(call: Call<RecipeDetailsResponse>, t: Throwable) {
                    errorToast(
                        this@MealPlannerActivity,
                        "Error: ${t.message}")
                }
            })
    }

    private fun saveFullMealPlanToFirebase() {

        if (selectedDate.isEmpty()) {
            warningToast(this, "Please select a date")
            return
        }

        if (selectedMeals.isEmpty()) {
            warningToast(this, "No meals selected")
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            errorToast(this, "User not logged in")
            return
        }

        val database = FirebaseDatabase.getInstance()
        val mealRef =
            database.getReference("users").child(user.uid).child("meal_plans").child(selectedDate)

        val mealPlanData = mutableMapOf<String, Any>()

        selectedMeals.forEach { (mealType, meals) ->
            val mealList = meals.map { meal ->
                hashMapOf(
                    "id" to meal.id,
                    "title" to meal.title,
                    "image" to meal.image,
                    "readyInMinutes" to meal.readyInMinutes,
                    "healthScore" to meal.healthScore,
                    "note" to meal.note,
                    "kcal" to meal.kcal,
                    "carbs" to meal.carbs,
                    "fat" to meal.fat,
                    "protein" to meal.protein
                )
            }
            mealPlanData[mealType] = mealList
        }
        val msg = "Meal Planned Save Successfully! "
        note.text.clear()
        successToast(this,msg)

        mealRef.setValue(mealPlanData)
            .addOnSuccessListener {
                note.text.clear()
                successToast(this,msg)
                selectedMeals.clear()
            }
            .addOnFailureListener { e ->
                errorToast(this, "Failed to save: ${e.message}")
            }
    }
}