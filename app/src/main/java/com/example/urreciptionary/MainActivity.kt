package com.example.urreciptionary

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var appNameText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        progressBar = findViewById(R.id.progressBar)
        appNameText = findViewById(R.id.appNameText)

        appNameText.animate().alpha(1f).setDuration(1000).start()

        startProgressBar()
    }

    private fun startProgressBar() {
        val handler = Handler(Looper.getMainLooper())
        val updateProgressRunnable = object : Runnable {
            var progress = 0

            override fun run() {
                if (progress <= 100) {
                    progressBar.progress = progress
                    progress++
                    handler.postDelayed(this, 10)
                } else {
//                     - FoodDetectionActivity HomeActivity
                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    val options = ActivityOptionsCompat.makeCustomAnimation(
                        this@MainActivity,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )
                    startActivity(intent, options.toBundle())
                    finish()
                }
            }
        }
        handler.post(updateProgressRunnable)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}