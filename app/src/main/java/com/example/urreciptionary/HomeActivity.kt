package com.example.urreciptionary

import CustomSmoothScroller
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.urreciptionary.network.ApiService
import com.example.urreciptionary.network.RecipeDetailsResponse
import com.example.urreciptionary.network.RecipeResponse
import com.example.urreciptionary.network.RecipeWithDetails
import com.example.urreciptionary.network.RetrofitClient
import com.example.urreciptionary.viewmodel.PopularFoodsViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.abs

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class HomeActivity : BaseActivity() {
    private var currentPage = 2
    private val pageSize = 2
    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: PopularRecipeAdapter
    private lateinit var shimmerLayout: ShimmerFrameLayout
    private lateinit var popularShowMoreText: MaterialButton
    private lateinit var logoutBtn: ImageButton
    private lateinit var profileImage: ImageView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var appBarLayout: AppBarLayout
    private lateinit var viewModel: PopularFoodsViewModel

    private val apiKey = BuildConfig.API_KEY

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setActivityLayout(R.layout.activity_home)
        initializeBottomNavigation()
        setupBackButton()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        viewModel = ViewModelProvider(this).get(PopularFoodsViewModel::class.java)

        val user = FirebaseAuth.getInstance().currentUser
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val greetingText = findViewById<TextView>(R.id.gretting)
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        profileImage = findViewById(R.id.profileImage)
        logoutBtn = findViewById(R.id.logout)
        profileImage.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }
        if (user != null) {
            val uid = user.uid
            val homeUsername = findViewById<TextView>(R.id.drawer_menu_username)

            val usernameRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("username")

            usernameRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val username = snapshot.getValue(String::class.java)
                        homeUsername.text = username
                    } else {
                        homeUsername.text = "SmartChef - User"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Failed to read username", error.toException())
                }

            })

            val photoRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("photoUrl")

            photoRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val photoUrl = snapshot.getValue(String::class.java)
                        Glide.with(this@HomeActivity)
                            .load(photoUrl)
                            .circleCrop()
                            .placeholder(R.drawable.pf_img_nobg)
                            .into(profileImage)
                    } else {
                        profileImage.setImageResource(R.drawable.pf_img_nobg)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Failed to read profile image", error.toException())
                }

            })
        }

        val greeting = when (hour) {
            in 0..11 -> "Good Morning!"
            in 12..16 -> "Good Afternoon"
            else -> "\uD83C\uDF19 Good evening"
        }


        greetingText.text = greeting

        logoutBtn.setOnClickListener {
            logoutUser()
        }

        appBarLayout = findViewById(R.id.materialCardView)

        popularShowMoreText = findViewById(R.id.popularShowMoreText)
        popularShowMoreText.setOnClickListener {
            openCategoryActivity("", "POPULAR FOODS", "popularity")
        }

        appBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            val elevation = if (abs(verticalOffset) == appBarLayout.totalScrollRange) 8f else 2f
        }

        val breakfastCard = findViewById<MaterialCardView>(R.id.breakfast_id)
        val dessertCard = findViewById<MaterialCardView>(R.id.dessert_id)
        val lunchCard = findViewById<MaterialCardView>(R.id.lunch_id)
        val dinnerCard = findViewById<MaterialCardView>(R.id.dinner_id)
        val drinkCard = findViewById<MaterialCardView>(R.id.drink_id)

        breakfastCard.setOnClickListener {
            openCategoryActivity("breakfast", "BREAKFAST", "")
        }

        dessertCard.setOnClickListener {
            openCategoryActivity("dessert", "DESSERT", "")
        }

        lunchCard.setOnClickListener {
            openCategoryActivity("lunch", "LUNCH", "")
        }

        dinnerCard.setOnClickListener {
            openCategoryActivity("dinner", "DINNER", "")
        }

        drinkCard.setOnClickListener {
            openCategoryActivity("drink", "DRINKS", "")
        }


        recyclerView = findViewById(R.id.recyclerView)
        shimmerLayout = findViewById(R.id.shimmer_layout)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.setHasFixedSize(true)

        shimmerLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        popularShowMoreText.visibility = View.GONE

        viewModel.popularRecipes.observe(this) { recipes ->
            if (recipes.isNotEmpty()) {
                val recipeAdapter = PopularRecipeAdapter(recipes)
                recyclerView.adapter = recipeAdapter

                shimmerLayout.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                popularShowMoreText.visibility = View.VISIBLE
            } else {
                warningToast(this, "No popular recipes found")
            }
        }

        if (!viewModel.hasLoaded) {
            viewModel.loadPopularRecipe(apiKey, currentPage, pageSize)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private fun openCategoryActivity(category: String, categoryName: String, popularity: String) {
        val intent = Intent(this, CategoryItemActivity::class.java)
        intent.putExtra("CATEGORY", category)
        intent.putExtra("CATEGORY_NAME", categoryName)
        intent.putExtra("POPULARITY", popularity)
        startActivity(intent)
    }

    private fun Float.dpToPx(context: Context): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            context.resources.displayMetrics
        )
    }
}