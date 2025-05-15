package com.example.urreciptionary

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.urreciptionary.network.Meal
import com.github.lzyzsd.circleprogress.DonutProgress
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class SaveMealPlansActivity : BaseActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var mealPlansRef: DatabaseReference
    private lateinit var layoutCalender: LinearLayout
    private var selectedDay: View? = null
    private var selectedDate: String? = null
    private var mealType: String? = null
    private var isFirstDayLoaded = false

    private var lastSelectedMealType: String = "breakfast"
    private lateinit var kcalValue: TextView
    private lateinit var proteinShow: TextView
    private lateinit var carbsShow: TextView
    private lateinit var fatShow: TextView

    private lateinit var btnComplete: Chip
    private lateinit var btnIncomplete: Chip
    private lateinit var deleteBtnFood: Chip

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setActivityLayout(R.layout.activity_save_meal_plans)

        database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        mealPlansRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId!!)
            .child("meal_plans")
        layoutCalender = findViewById(R.id.layoutCalendar)

        val tvMonth = findViewById<TextView>(R.id.tvMonth)
        val currentDate = LocalDate.now()
        val monthName = currentDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val year = currentDate.year
        tvMonth.text = "$monthName $year"

        val btnForBreakfast = findViewById<Button>(R.id.btnBreakfast)
        val btnForLunch = findViewById<Button>(R.id.btnLunch)
        val btnForDinner = findViewById<Button>(R.id.btnDinner)

        btnForBreakfast.setOnClickListener {
            selectedDate?.let {
                mealType = "breakfast"
                loadMeals(it,"breakfast")
            }
            setSelectedTab(btnForBreakfast)
        }

        btnForLunch.setOnClickListener {
            selectedDate?.let {
                mealType = "lunch"
                loadMeals(it,"lunch")
            }
            setSelectedTab(btnForLunch)
        }

        btnForDinner.setOnClickListener {
            selectedDate?.let {
                mealType = "dinner"
                loadMeals(it,"dinner")
            }
            setSelectedTab(btnForDinner)
        }
        
        val btnGoToPlanner = findViewById<Button>(R.id.btnGoToPlanner)
        val btnGoToPlannerRedirect = findViewById<Button>(R.id.goToMealPlan)

        btnGoToPlanner.setOnClickListener {
            val intent = Intent(this@SaveMealPlansActivity, MealPlannerActivity::class.java)
            startActivity(intent)
        }

        btnGoToPlannerRedirect.setOnClickListener {
            val intent = Intent(this@SaveMealPlansActivity, MealPlannerActivity::class.java)
            startActivity(intent)
        }

        val bottomSheet = findViewById<LinearLayout>(R.id.saveMealData)
        val behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.peekHeight = 285

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        kcalValue = findViewById(R.id.kcalValue)
        proteinShow = findViewById(R.id.proteinShow)
        carbsShow = findViewById(R.id.carbsShow)
        fatShow = findViewById(R.id.fatShow)

        applyBlurTo(proteinShow)
        applyBlurTo(fatShow)
        applyBlurTo(carbsShow)

        btnComplete = findViewById(R.id.btnComplete)
        btnIncomplete = findViewById(R.id.btnIncomplete)
        deleteBtnFood = findViewById(R.id.btnDelete)

        btnComplete.setOnClickListener {
            val recyclerView = findViewById<RecyclerView>(R.id.rvMeals)
            val adapter = recyclerView.adapter as? MealPlanAdapter
            val selectedMeals = adapter?.getSelectedMeals() ?: return@setOnClickListener
            val date  = selectedDate ?: return@setOnClickListener
            val mealType = lastSelectedMealType

            val updates = mutableMapOf<String, Any?>()
            for (meal in selectedMeals) {
                val firebaseKey = meal.firebaseKey ?: continue
                val path = "$date/$mealType/$firebaseKey/completed"
                updates[path] = true
            }

            mealPlansRef.updateChildren(updates)
                .addOnSuccessListener {
                    adapter.markMealsCompleted(selectedMeals.map { it.id.toString() })
                    btnIncomplete.visibility = View.VISIBLE
                    btnComplete.visibility = View.GONE
                    val message = "You crushed that meal! ☺️"
                    successToast(this, message)

                    loadAndDisplayMacrosForDate(date)
                }
                .addOnFailureListener {
                    errorToast(this, "Failed to mark completed")
                }
        }

        btnIncomplete.setOnClickListener {
            val recyclerView = findViewById<RecyclerView>(R.id.rvMeals)
            val adapter = recyclerView.adapter as? MealPlanAdapter
            val selectedMeals = adapter?.getSelectedMeals() ?: return@setOnClickListener
            val date = selectedDate ?: return@setOnClickListener
            val mealType = lastSelectedMealType

            val updates = mutableMapOf<String, Any?>()
            for (meal in selectedMeals) {
                val firebaseKey = meal.firebaseKey ?: continue
                val path = "$date/$mealType/$firebaseKey/completed"
                updates[path] = false
            }

            mealPlansRef.updateChildren(updates)
                .addOnSuccessListener {
                    val message = "Oops — changed your mind 👍"
                    successToast(this, message)
                    adapter.markMealsIncomplete(selectedMeals.map { it.id.toString() })
                    btnComplete.visibility = View.VISIBLE
                    btnIncomplete.visibility = View.GONE

                    loadAndDisplayMacrosForDate(date)
                }
                .addOnFailureListener {
                    errorToast(this, "Failed to mark as incomplete")
                }
        }

        deleteBtnFood.setOnClickListener {
            val recyclerView = findViewById<RecyclerView>(R.id.rvMeals)
            val adapter = recyclerView.adapter as? MealPlanAdapter
            val selectedMeals = adapter?.getSelectedMeals() ?: emptyList()

            if (selectedMeals.isEmpty()) {
                val message = "Please Select Meal!"
                deleteToast(this, message)
                return@setOnClickListener
            }

            val builder = AlertDialog.Builder(this)
            builder.setMessage("Are you sure you want to delete this meal?")
                .setCancelable(false)
                .setPositiveButton("Yes") { _, _ ->

                    if (selectedDate == null) {
                        return@setPositiveButton
                    }

                    val type = lastSelectedMealType

                    var deletedCount = 0
                    val totalToDelete = selectedMeals.size

                    selectedMeals.forEach { meal ->
                        deleteMealFromFirebase(selectedDate!!, type, meal) {
                            deletedCount++
                            if (deletedCount == totalToDelete) {
                                adapter?.clearSelection()
                                loadMeals(selectedDate!!, type)
                                val message = "Wiped from your list! 🫡"
                                deleteToast(this, message)
                            }
                        }
                    }
                }
                .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

            builder.create().show()
        }

        fetchMealPlanDates()

    }

    private fun deleteMealFromFirebase(date: String, mealType: String, meal: Meal, onComplete: () -> Unit) {
        val firebaseKey = meal.firebaseKey ?: run {
            return
        }

        val path = "$date/$mealType/$firebaseKey"

        mealPlansRef.child(path).removeValue()
            .addOnSuccessListener {
                onComplete()
            }
            .addOnFailureListener {
            }
    }

    private fun applyBlurTo(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blurEffect = RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
            view.setRenderEffect(blurEffect)
        }
    }

    override fun onResume() {
        super.onResume()
        fetchMealPlanDates()
    }

    private fun fetchMealPlanDates() {
        mealPlansRef.addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                layoutCalender.removeAllViews()
                val emptyStateView = findViewById<LinearLayout>(R.id.emptyStateView)
                val scrollViewMeals = findViewById<NestedScrollView>(R.id.mainScrollView)
                val bottomSheet = findViewById<LinearLayout>(R.id.saveMealData)

                val dates = snapshot.children.mapNotNull { it.key }

                val futureDates = dates
                    .mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }
                    .filter { !it.isBefore(LocalDate.now()) }
                    .sorted()

                if (futureDates.isEmpty()) {
                    emptyStateView.visibility = View.VISIBLE
                    scrollViewMeals.visibility = View.GONE
                    bottomSheet.visibility = View.GONE

                    return
                } else {
                    emptyStateView.visibility = View.GONE
                    scrollViewMeals.visibility = View.VISIBLE
                    bottomSheet.visibility = View.VISIBLE
                }

                for (date in futureDates) {
                    val dayContainer = createDayContainer(date.toString())
                    layoutCalender.addView(dayContainer)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createDayContainer(date: String): View {
        val dayDate = LocalDate.parse(date)
        val dayContainer = LayoutInflater.from(this)
            .inflate(R.layout.day_container_layout, null) as ConstraintLayout

        val dayName = dayDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        val dayNumber = dayDate.dayOfMonth.toString()

        val dayNameTextView = dayContainer.findViewById<TextView>(R.id.dayNameTextView)
        dayNameTextView.text = dayName

        val dayDateTextView = dayContainer.findViewById<TextView>(R.id.dayTextView)
        dayDateTextView.text = dayNumber

        val dayFrame = dayContainer.findViewById<FrameLayout>(R.id.dayFrame)

        dayFrame.setOnClickListener {
            selectDay(dayContainer, dayFrame, dayDate)
        }

        if (!isFirstDayLoaded) {
            selectDay(dayContainer, dayFrame, dayDate)
            isFirstDayLoaded = true
        }

        return dayContainer
    }

    private fun selectDay(dayContainer: View, dayFrame: FrameLayout, dayDate: LocalDate) {
        selectedDay?.findViewById<FrameLayout>(R.id.dayFrame)
            ?.setBackgroundResource(R.drawable.bg_calendar_day_default)

        dayFrame.setBackgroundResource(R.drawable.bg_calendar_day_selected)
        selectedDay = dayContainer

        val clickedDate = dayDate.toString()
        selectedDate = clickedDate

        val btnBreakfast = findViewById<Button>(R.id.btnBreakfast)
        loadMeals(clickedDate, "breakfast")
        loadAndDisplayMacrosForDate(clickedDate)
        setSelectedTab(btnBreakfast)


        val springX = SpringAnimation(dayContainer, SpringAnimation.SCALE_X).apply {
            spring = SpringForce(1f).apply {
                dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                stiffness = SpringForce.STIFFNESS_LOW
            }
        }
        springX.setStartValue(1.2f)

        val springY = SpringAnimation(dayContainer, SpringAnimation.SCALE_Y).apply {
            spring = SpringForce(1f).apply {
                dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                stiffness = SpringForce.STIFFNESS_LOW
            }
        }
        springY.setStartValue(1.2f)

        springX.start()
        springY.start()
    }

    private fun loadAndDisplayMacrosForDate(date: String) {
        val dayRef = mealPlansRef.child(date)

        dayRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalKcal = 0
                var totalProtein = 0
                var totalFat = 0
                var totalCarbs = 0
                var mealCount = 0

                var totalItems = 0
                var completedKcal = 0
                var completedItems = 0

                for (mealTypeSnapshot in snapshot.children) {
                    for (mealSnapshot in mealTypeSnapshot.children) {
                        val kcal = mealSnapshot.child("kcal").getValue(Int::class.java) ?: 0
                        val protein = mealSnapshot.child("protein").getValue(Int::class.java) ?: 0
                        val fat = mealSnapshot.child("fat").getValue(Int::class.java) ?: 0
                        val carbs = mealSnapshot.child("carbs").getValue(Int::class.java) ?: 0

                        totalKcal += kcal
                        totalProtein += protein
                        totalFat += fat
                        totalCarbs += carbs
                        mealCount++
                    }
                }

                for (mealSnapshot in snapshot.children) {
                    for (foodSnapshot in mealSnapshot.children) {
                        val foodData = foodSnapshot.value as? Map<*, *> ?: continue

                        val kcal = (foodData["kcal"] as? Long)?.toInt() ?: 0
                        val carbs = (foodData["carbs"] as? Long)?.toInt() ?: 0
                        val protein = (foodData["protein"] as? Long)?.toInt() ?: 0
                        val fat = (foodData["fat"] as? Long)?.toInt() ?: 0
                        val completed = foodData["completed"] as? Boolean ?: false

                        totalCarbs += carbs
                        totalProtein += protein
                        totalFat += fat

                        totalItems++

                        if (completed) {
                            completedItems++
                            completedKcal += kcal
                        }
                    }
                }

                val totalMacros = totalProtein + totalFat + totalCarbs
                val proteinPercent = if (totalMacros > 0) (totalProtein * 100) / totalMacros else 0
                val fatPercent = if (totalMacros > 0) (totalFat * 100) / totalMacros else 0
                val carbsPercent = if (totalMacros > 0) (totalCarbs * 100) / totalMacros else 0

                kcalValue.text = "$totalKcal"
                proteinShow.text = "Protein\n${proteinPercent}g"
                carbsShow.text = "Carbs\n${carbsPercent}g"
                fatShow.text = "Fat\n${fatPercent}g"

                btnComplete.text = "$completedItems of $totalItems completed"

                val meterView = findViewById<DonutProgress>(R.id.progressMeter)
                val percent = if (totalKcal > 0) (completedKcal.toFloat() / totalKcal * 100f) else 0f
                val safePercent = percent.coerceIn(0f, 100f)

                meterView.progress = safePercent
                meterView.text = "$completedKcal / $totalKcal kcal"
                meterView.invalidate()
                meterView.requestLayout()

                ObjectAnimator.ofFloat(meterView, "progress", 0f, safePercent).apply {
                    duration = 1000
                    interpolator = DecelerateInterpolator()
                    start()
                }


                val completionText = findViewById<TextView>(R.id.completedMealsText)
                val remainingText = findViewById<TextView>(R.id.remainingMealsText)
                val remaining = totalItems - completedItems
                completionText.text = "$completedItems of $totalItems completed"
                remainingText.text = "$remaining remaining"
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadMeals(date: String , mealType: String){
        val mealRef = mealPlansRef.child(date).child(mealType)
        val recyclerView = findViewById<RecyclerView>(R.id.rvMeals)
        val emptyMealImage = findViewById<ImageView>(R.id.ivEmptyMeal)
        val actionLayout = findViewById<LinearLayout>(R.id.actionLayout)

        val slideOut = TranslateAnimation(0f, -recyclerView.width.toFloat(), 0f, 0f).apply {
            duration = 200
            fillAfter = true
        }


        val mealTypes = listOf("breakfast", "lunch", "dinner")
        val currentIndex = mealTypes.indexOf(mealType)
        val lastIndex = mealTypes.indexOf(lastSelectedMealType)

        val slideOutDirection = if (currentIndex > lastIndex) -1 else 1
        val slideInDirection = -slideOutDirection

        slideOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                mealRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val mealList = mutableListOf<Meal>()
                        for (mealSnapshot in snapshot.children) {
                            val meal = mealSnapshot.getValue(Meal::class.java)
                            meal?.setFirebaseKey(mealSnapshot.key)
                            meal?.let { mealList.add(it) }
                        }

                        recyclerView.layoutManager = LinearLayoutManager(this@SaveMealPlansActivity)
                        recyclerView.adapter = MealPlanAdapter(mealList, actionLayout)

                        if (mealList.isEmpty()) {
                            emptyMealImage.visibility = View.VISIBLE
                        } else {
                            emptyMealImage.visibility = View.GONE
                        }

                        val slideIn = TranslateAnimation(recyclerView.width.toFloat(), 0f, 0f, 0f).apply {
                            duration = 250
                        }
                        recyclerView.startAnimation(slideIn)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        recyclerView.startAnimation(slideOut)
        lastSelectedMealType = mealType
    }

    private fun setSelectedTab(selectedButton: Button) {
        val buttons = listOf(
            findViewById<Button>(R.id.btnBreakfast),
            findViewById<Button>(R.id.btnLunch),
            findViewById<Button>(R.id.btnDinner)
        )

        buttons.forEach { button ->
            if (button == selectedButton) {
                button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_light)
                button.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            } else {
                button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.white_smoke)
                button.setTextColor(ContextCompat.getColor(this, android.R.color.black))
            }
        }
    }
}