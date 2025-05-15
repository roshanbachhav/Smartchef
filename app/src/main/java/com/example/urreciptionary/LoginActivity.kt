package com.example.urreciptionary

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import jp.wasabeef.blurry.Blurry
import pl.droidsonroids.gif.GifImageView

class LoginActivity : BaseActivity() {
    private lateinit var editEmail: TextInputEditText
    private lateinit var editPassword: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var redirectOnSignUp: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore
    private lateinit var loadingGif: GifImageView
    private lateinit var blurOverlay: ImageView
    private lateinit var rootView: ViewGroup
    private lateinit var btnGoogleLogin: MaterialButton

    private val RC_GOOGLE_SIGN_IN = 1001


    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        database = FirebaseDatabase.getInstance()
        loadingGif = findViewById(R.id.login_loading)
        blurOverlay = findViewById(R.id.login_blur_overlay)
        rootView = findViewById(android.R.id.content)

        editEmail = findViewById<TextInputLayout>(R.id.textLoginEmail).editText as TextInputEditText
        editPassword =
            findViewById<TextInputLayout>(R.id.textLoginPassword).editText as TextInputEditText


        redirectOnSignUp = findViewById(R.id.redirectToSignUp)
        redirectOnSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)

            val options = ActivityOptions.makeCustomAnimation(
                this, R.anim.fade_in, R.anim.fade_out
            )
            startActivity(intent, options.toBundle())
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }

        val redirectOnForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)
        redirectOnForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            val options = ActivityOptions.makeCustomAnimation(
                this, R.anim.fade_in, R.anim.fade_out
            )
            startActivity(intent, options.toBundle())
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }

        loginButton = findViewById(R.id.btnLogin)
        loginButton.setOnClickListener {
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()

            editEmail.error = null
            editPassword.error = null

            if (email.isEmpty()) {
                editEmail.error = "Email cannot be empty"
            } else if (!isValidEmail(email)) {
                editEmail.error = "Invalid email format"
            } else if (password.isEmpty()) {
                editPassword.error = "Password cannot be empty"
            } else if (!isValidPassword(password)) {
                editPassword.error = "Password must be at least 6 characters"
            } else {
                showLoading(true)
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            showLoading(false)
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val exception = task.exception
                            if (exception is FirebaseAuthInvalidCredentialsException) {
                                if (exception.message?.contains("The password is invalid") == true) {
                                    errorToast(baseContext, "Incorrect password.")
                                    showLoading(false)
                                } else if (exception.message?.contains("There is no user record") == true) {
                                    errorToast(baseContext, "No user found with this email.")
                                    showLoading(false)
                                } else {
                                    errorToast(
                                        baseContext,
                                        "Authentication failed. Please try again."
                                    )
                                    showLoading(false)
                                }
                            } else if (exception is FirebaseAuthInvalidUserException) {
                                errorToast(baseContext, "No user found with this email.")
                            } else {
                                errorToast(baseContext, "Authentication failed. Please try again.")
                            }
                        }
                    }
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)
        btnGoogleLogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                errorToast(this, "Google sign-in failed $e")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credentials = GoogleAuthProvider.getCredential(idToken, null)
        showLoading(true)

        auth.signInWithCredential(credentials)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userMap = hashMapOf(
                        "username" to (user?.displayName ?: "Google User"),
                        "email" to (user?.email ?: "")
                    )

                    user?.let {
                        database.reference.child("users").child(it.uid)
                            .setValue(userMap)
                            .addOnSuccessListener {
                                successToast(this, "Signed in with Google!")
                                showLoading(false)
                                startActivity(Intent(this, SaveMealPlansActivity::class.java))
                                finish()
                            }

                            .addOnFailureListener { e ->
                                errorToast(this, "Failed to save user info $e")
                                showLoading(false)
                            }
                    }
                } else {
                    Log.w("SignupActivity", "signInWithCredential:failure", task.exception)
                    errorToast(this, "Firebase Auth failed")
                    showLoading(false)
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        val regex = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        return email.matches(regex.toRegex())
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6

    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            loadingGif.visibility = View.VISIBLE
            blurOverlay.visibility = View.VISIBLE

            Blurry.with(this)
                .radius(20)
                .sampling(2)
                .async()
                .capture(rootView)
                .into(blurOverlay)
        } else {
            loadingGif.visibility = View.GONE
            blurOverlay.visibility = View.GONE
        }
    }

}