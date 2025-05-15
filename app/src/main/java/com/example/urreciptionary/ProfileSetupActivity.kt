package com.example.urreciptionary

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProfileSetupActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setActivityLayout(R.layout.activity_profile_setup)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, SetProfileFragment())
            .commit()
    }
}