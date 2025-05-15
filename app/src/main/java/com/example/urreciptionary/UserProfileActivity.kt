package com.example.urreciptionary

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserProfileActivity : BaseActivity() {
    private lateinit var profileImage: ShapeableImageView
    private lateinit var username: TextView
    private lateinit var email: TextView
    private lateinit var age: TextView
    private lateinit var gender: TextView
    private lateinit var weight: TextView
    private lateinit var height: TextView
    private lateinit var editProfile: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setActivityLayout(R.layout.activity_user_profile)

        initializeBottomNavigation()
        setupBackButton()

        editProfile = findViewById(R.id.editProfile)
        editProfile.setOnClickListener {
            val intent = Intent(this, ProfileSetupActivity::class.java)
            startActivity(intent)
        }

        profileImage = findViewById(R.id.profileImage)
        username = findViewById(R.id.username)
        email = findViewById(R.id.email)
        age = findViewById(R.id.ageShow)
        gender = findViewById(R.id.genderShow)
        weight = findViewById(R.id.weightShow)
        height = findViewById(R.id.heightShow)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val database = FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(uid)

            database.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val usernameText = snapshot.child("username").value.toString()
                    val emailText = snapshot.child("email").value.toString()
                    val ageText = snapshot.child("age").value?.toString() ?: "Not specified"
                    val genderText = snapshot.child("gender").value?.toString() ?: "Not specified"
                    val weightText = snapshot.child("weight").value?.toString() ?: "0.0"
                    val heightText = snapshot.child("height").value?.toString() ?: "0.0"
                    val photoUrl = snapshot.child("photoUrl").value.toString()

                    username.text = usernameText
                    email.text = emailText
                    age.text = ageText
                    gender.text = genderText
                    weight.text = weightText
                    height.text = heightText

                    if (!isDestroyed && !isFinishing && photoUrl != null) {
                        Glide.with(this)
                            .load(photoUrl)
                            .circleCrop()
                            .placeholder(R.drawable.pf_img_nobg)
                            .into(profileImage)
                    } else {
                        profileImage.setImageResource(R.drawable.pf_img_nobg)
                    }
                }
            }
                .addOnFailureListener {
                    errorToast(this, "Failed to load user data")
                }
        }
    }
}