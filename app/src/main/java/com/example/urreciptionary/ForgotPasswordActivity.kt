package com.example.urreciptionary

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : BaseActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var resetBtn: MaterialButton
    private lateinit var backToLoginBtn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        val emailInput = findViewById<EditText>(R.id.emailInput)
        resetBtn = findViewById(R.id.resetButton)
        resetBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()

            if(TextUtils.isEmpty(email)){
                emailInput.error = "Please enter your email address"
                return@setOnClickListener
            }
            sendPasswordResetEmail(email)
        }

        backToLoginBtn = findViewById(R.id.backToLogin)
        backToLoginBtn.setOnClickListener {
            val intent = Intent(this , LoginActivity::class.java)
            startActivity(intent)
        }

    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener(this) { t ->
                if(t.isSuccessful){
                    successToast(this, "Reset link sent at $email")
                }else{
                    errorToast(this, "Failed to send reset email")
                }
            }
    }
}