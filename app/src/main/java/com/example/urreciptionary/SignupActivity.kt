package com.example.urreciptionary

import android.app.ActivityOptions
import android.content.ContentValues.TAG
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import jp.wasabeef.blurry.Blurry
import pl.droidsonroids.gif.GifImageView

class SignupActivity : BaseActivity() {

    private lateinit var editUsername: TextInputEditText
    private lateinit var editEmail: TextInputEditText
    private lateinit var editPassword: TextInputEditText
    private lateinit var redirectOnLogin: TextView
    private lateinit var signupButton: MaterialButton
    private lateinit var btnGoogleLogin: MaterialButton
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var database: FirebaseDatabase
    private lateinit var loadingGif: GifImageView
    private lateinit var blurOverlay: ImageView
    private lateinit var rootView: ViewGroup

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
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        database = FirebaseDatabase.getInstance()
        loadingGif = findViewById(R.id.loading)
        blurOverlay = findViewById(R.id.blur_overlay)
        rootView = findViewById(android.R.id.content)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        editUsername =
            findViewById<TextInputLayout>(R.id.textUsername).editText as TextInputEditText
        editEmail = findViewById<TextInputLayout>(R.id.textEmail).editText as TextInputEditText
        editPassword =
            findViewById<TextInputLayout>(R.id.textPassword).editText as TextInputEditText


        redirectOnLogin = findViewById(R.id.redirectToLogin)
        redirectOnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)

            val options = ActivityOptions.makeCustomAnimation(
                this, R.anim.fade_in, R.anim.fade_out
            )
            startActivity(intent, options.toBundle())
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }

        signupButton = findViewById(R.id.signUpButton)
        signupButton.setOnClickListener {
            val username = editUsername.text.toString()
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()

            editUsername.error = null
            editEmail.error = null
            editPassword.error = null

            if (username.isEmpty()) {
                editUsername.error = "Username cannot be empty"
            } else if (email.isEmpty()) {
                editEmail.error = "Email cannot be empty"
            } else if (!isValidEmail(email)) {
                editEmail.error = "Invalid email format"
            } else if (password.isEmpty()) {
                editPassword.error = "Password cannot be empty"
            } else if (!isValidPassword(password)) {
                editPassword.error = "Password must be at least 6 characters"
            } else {
                showLoading(true)
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->

                        if (task.isSuccessful) {
                            Log.d("SignupActivity", "User creation successful!")
                            val user = auth.currentUser
                            if (user != null) {
                                Log.d("SignupActivity", "User created: ${user.uid}")

                                val userMap = hashMapOf(
                                    "username" to username,
                                    "email" to email
                                )

                                database.reference.child("users").child(user.uid)
                                    .setValue(userMap)
                                    .addOnSuccessListener {
                                        successToast(this,"User created successfully")
                                        showLoading(false)
                                        val intent = Intent(this, HomeActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(
                                            "SignupActivity",
                                            "Error saving user data to Realtime Database",
                                            e
                                        )
                                        Toast.makeText(
                                            this,
                                            "Error saving user data",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                Log.e(
                                    "SignupActivity",
                                    "User creation failed, current user is null."
                                )
                            }
                        } else {
                            val exception = task.exception
                            if (exception is FirebaseAuthException) {
                                Log.e(
                                    "SignupActivity",
                                    "Authentication failed. Error code: ${exception.errorCode}, Message: ${exception.message}"
                                )
                            } else {
                                Log.e(
                                    "SignupActivity",
                                    "Unknown error during authentication",
                                    exception
                                )
                                Toast.makeText(
                                    this,
                                    "Unknown error occurred: ${exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
            }
        }

        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)
        btnGoogleLogin.setOnClickListener {
            val singInIntent = googleSignInClient.signInIntent
            startActivityForResult(singInIntent , RC_GOOGLE_SIGN_IN)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_GOOGLE_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            }catch (e: ApiException){
                errorToast(this, "Google sign-in failed $e")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String){
        val credentials = GoogleAuthProvider.getCredential(idToken , null)
        showLoading(true)

        auth.signInWithCredential(credentials)
            .addOnCompleteListener(this){ task ->
                if(task.isSuccessful){
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
                                val intent = Intent(this, ProfileSetupActivity::class.java)
                                startActivity(intent)
                            }

                            .addOnFailureListener { e ->
                                errorToast(this, "Failed to save user info $e")
                                showLoading(false)
                            }
                    }
                }else{
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