package com.example.urreciptionary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

abstract class BaseActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    protected lateinit var drawerLayout: DrawerLayout
    protected lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    companion object {
        private var isMediaManagerInitialized = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_base_drawer)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        if (!isMediaManagerInitialized) {
            val config = mapOf(
                "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
            )
            MediaManager.init(this, config)
            isMediaManagerInitialized = true
        }

        val headerView = navigationView.getHeaderView(0)
        val drawerUsername = headerView.findViewById<TextView>(R.id.drawer_menu_username)
        val drawerEmail = headerView.findViewById<TextView>(R.id.drawer_menu_email)
        val profileImage = headerView.findViewById<ImageView>(R.id.profileImage)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null){
            val uid = user.uid

            val usernameRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("username")

            usernameRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val username = snapshot.getValue(String::class.java)
                        drawerUsername.text = username
                    }else{
                        drawerUsername.text = "SmartChef - User"
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
                        if (!isDestroyed && !isFinishing && photoUrl != null) {
                            Glide.with(this@BaseActivity)
                                .load(photoUrl)
                                .circleCrop()
                                .placeholder(R.drawable.pf_img_nobg)
                                .into(profileImage)
                        }
                    } else {
                        profileImage.setImageResource(R.drawable.pf_img_nobg)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Failed to read profile image", error.toException())
                }
            })

        }else {
            drawerUsername.text = "Guest"
            profileImage.setImageResource(R.drawable.pf_img_nobg)
        }

        val email = user?.email
        if (email != null) {
            drawerEmail.text = email
        } else {
            drawerEmail.text = "Email not available"
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupDrawerNavigation()
    }

    protected fun setActivityLayout(layoutResId: Int) {
        val contentFrame = findViewById<FrameLayout>(R.id.contentFrame)
        layoutInflater.inflate(layoutResId, contentFrame)
    }

    private fun setupDrawerNavigation() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> startNewActivity(HomeActivity::class.java)
                R.id.nav_meal_planner -> startNewActivity(SaveMealPlansActivity::class.java)
                R.id.nav_popular -> startNewActivity(FavoriteFoodActivity::class.java)
//                R.id.nav_notes -> startNewActivity(NotesActivity::class.java)
//                R.id.nav_settings -> startNewActivity(SettingsActivity::class.java)
                R.id.nav_logout -> logoutUser()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun startNewActivity(activityClass: Class<*>) {
        if (this::class.java != activityClass) {
            val intent = Intent(this, activityClass)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    fun logoutUser() {
        Firebase.auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut().addOnCompleteListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    protected fun initializeBottomNavigation(setSelected: Boolean = true) {
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    if (this !is HomeActivity) {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.cooking -> {
                    if (this !is CookingActivity) {
                        startActivity(Intent(this, CookingActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.recipe -> {
                    if (this !is MealPlannerActivity) {
                        startActivity(Intent(this, MealPlannerActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.userProfile -> {
                    if (this !is UserProfileActivity) {
                        startActivity(Intent(this, UserProfileActivity::class.java))
                        finish()
                    }
                    true
                }
                else -> false
            }
        }

        if (setSelected) {
            bottomNavigationView.selectedItemId = when (this::class.java) {
                HomeActivity::class.java -> R.id.home
                CookingActivity::class.java -> R.id.cooking
                MealPlannerActivity::class.java -> R.id.recipe
                UserProfileActivity::class.java -> R.id.userProfile
                else -> -1
            }
        }
    }

    fun setupBackButton() {
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun successToast(context: Context, message: String){
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.success_toast , null)
        val text = layout.findViewById<TextView>(R.id.toastText)
        text.text = message
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_LONG
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
        toast.view = layout
        toast.show()
    }

    fun errorToast(context: Context, message: String){
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.error_toast , null)
        val text = layout.findViewById<TextView>(R.id.errorToastText)
        text.text = message
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_LONG
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
        toast.view = layout
        toast.show()
    }

    fun warningToast(context: Context, message: String){
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.warning_toast , null)
        val text = layout.findViewById<TextView>(R.id.warningToastText)
        text.text = message
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_LONG
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
        toast.view = layout
        toast.show()
    }

    fun deleteToast(context: Context, message: String){
        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.success_toast , null)
        val text = layout.findViewById<TextView>(R.id.toastText)
        text.text = message
        text.setTextColor(ContextCompat.getColor(context, R.color.red_500))

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_LONG
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 100)
        toast.view = layout
        toast.show()
    }


}
