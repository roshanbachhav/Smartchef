package com.example.urreciptionary

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.utils.ObjectUtils
import com.example.urreciptionary.utils.UserInfoManager
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SetProfileFragment : Fragment() {
    private var selectedImageUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    private var selectedView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imagePickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val imageUri = result.data?.data
                    selectedImageUri = imageUri
                    if (imageUri != null) {
                        view?.findViewById<ShapeableImageView>(R.id.ivProfileImage)
                            ?.setImageURI(imageUri)
                    }
                }
            }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_set_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        (activity as? BaseActivity)?.setupBackButton()

        val ivProfileImage = view.findViewById<ShapeableImageView>(R.id.ivProfileImage)

        UserInfoManager.getPhotoUrl { photoUrl ->
            if (!photoUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(photoUrl)
                    .circleCrop()
                    .placeholder(R.drawable.pf_img_nobg)
                    .into(ivProfileImage)
            } else {
                ivProfileImage.setImageResource(R.drawable.pf_img_nobg)
            }
        }

        super.onViewCreated(view, savedInstanceState)

        val btnSelectImage = view.findViewById<Button>(R.id.btnSelectImage)
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            imagePickerLauncher.launch(intent)
        }

        var selectedGenderView: ImageView? = null
        val genderViews = listOf(
            view.findViewById<ImageView>(R.id.ivMale),
            view.findViewById<ImageView>(R.id.ivFemale),
            view.findViewById<ImageView>(R.id.ivOther)
        )
        for (genderView in genderViews) {
            genderView.setOnClickListener {
                selectedGenderView?.isSelected = false

                genderView.isSelected = true
                selectedGenderView = genderView

                val gender = when (genderView.id) {
                    R.id.ivMale -> "Male"
                    R.id.ivFemale -> "Female"
                    else -> "Other"
                }
            }
        }


        val numberPickerContainer = view.findViewById<LinearLayout>(R.id.numberPickerContainer)
        val selectedBg = R.drawable.age_circle_selected
        val unselectedBg = R.drawable.age_circle_unselected

        for (i in 10..100) {
            val textView = TextView(requireContext()).apply {
                text = i.toString()
                textSize = 16f
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                background = ContextCompat.getDrawable(requireContext(), unselectedBg)

                val size = (64 * resources.displayMetrics.density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(12, 12, 12, 12)
                }

                setOnClickListener {
                    selectedView?.background = ContextCompat.getDrawable(context, unselectedBg)
                    background = ContextCompat.getDrawable(context, selectedBg)
                    selectedView = this
                }
            }

            numberPickerContainer.addView(textView)
        }

        loadUserProfile()

        val submitBtn = view.findViewById<Button>(R.id.btnContinue)
        submitBtn.setOnClickListener {
            val username = view.findViewById<EditText>(R.id.etUsername).text.toString()
            val weight = view.findViewById<EditText>(R.id.etWeight).text.toString()
            val height = view.findViewById<EditText>(R.id.etHeight).text.toString()
            val selectedAge = (selectedView as? TextView)?.text?.toString()?.toIntOrNull() ?: 0
            val baseActivity = requireActivity() as? BaseActivity

            when {
                username.isEmpty() -> {
                    baseActivity?.errorToast(requireContext(), "Please enter your name")
                    return@setOnClickListener
                }

                weight.isEmpty() -> {
                    baseActivity?.errorToast(requireContext(), "Please select your age")
                    return@setOnClickListener
                }

                height.isEmpty() -> {
                    baseActivity?.errorToast(requireContext(), "Please Enter Height")
                    return@setOnClickListener
                }

                selectedAge == 0 -> {
                    baseActivity?.errorToast(requireContext(), "Please Add Your age")
                    return@setOnClickListener
                }
            }

            val gender = when (selectedGenderView?.id) {
                R.id.ivMale -> "Male"
                R.id.ivFemale -> "Female"
                else -> "Other"
            }

            if (selectedImageUri != null) {
                uploadImageToCloudinary(selectedImageUri!!) { imageUrl ->
                    if (imageUrl != null) {
                        saveUserProfileToFirestore(
                            imageUrl,
                            username,
                            gender,
                            selectedAge,
                            weight,
                            height
                        )
                    } else {
                        (requireActivity() as? BaseActivity)?.errorToast(
                            requireContext(),
                            "Failed to upload image"
                        )
                    }

                }
            } else {
                saveUserProfileToFirestore("", username, gender, selectedAge, weight, height)
            }
        }
    }

    private fun uploadImageToCloudinary(imageUri: Uri, onComplete: (String?) -> Unit) {

        val baseActivity = requireActivity() as? BaseActivity

        val options = ObjectUtils.asMap(
            "folder", "profile_images",
            "public_id", FirebaseAuth.getInstance().currentUser?.uid
        )
        MediaManager.get().upload(imageUri).options(options).callback(object : UploadCallback {
            override fun onStart(requestId: String?) {

            }

            override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                val progressPercent = (bytes.toDouble() / totalBytes * 100).toInt()
                Log.d("CloudinaryUpload", "Upload progress: $progressPercent%")
            }

            override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                val imageUrl = resultData?.get("secure_url") as? String

                if (imageUrl != null) {
                    onComplete(imageUrl)
                } else {
                    baseActivity?.errorToast(
                        requireContext(),
                        "Something went wrong. Please try uploading the image again"
                    )
                    onComplete(null)
                }
            }

            override fun onError(requestId: String?, error: ErrorInfo?) {
                baseActivity?.errorToast(requireContext(), "something went wrong.")
                onComplete(null)
            }

            override fun onReschedule(requestId: String?, error: ErrorInfo?) {
            }
        }).dispatch()
    }

    private fun saveUserProfileToFirestore(
        imageUrl: String,
        username: String,
        gender: String,
        age: Int,
        weight: String,
        height: String,
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        val baseActivity = requireActivity() as? BaseActivity
        val db = FirebaseDatabase.getInstance().reference

        if (imageUrl.isEmpty()) {
            baseActivity?.errorToast(requireContext(), "Image URL is invalid.")
            return
        }
        val userProfile = mapOf(
            "photoUrl" to imageUrl,
            "username" to username,
            "gender" to gender,
            "age" to age,
            "weight" to weight,
            "height" to height
        )

        if (user != null) {
            db.child("users").child(user.uid).updateChildren(userProfile)
                .addOnSuccessListener {
                    baseActivity?.successToast(requireContext(), "Profile Updated!")
                    goToHome()
                }
                .addOnFailureListener {
                    baseActivity?.errorToast(
                        requireContext(),
                        "Something went wrong, Please try again"
                    )
                }
        }

    }

    private fun loadUserProfile() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val dbRef = FirebaseDatabase.getInstance().reference.child("users").child(user.uid)

        dbRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val photoUrl = snapshot.child("photoUrl").getValue(String::class.java)
                val gender = snapshot.child("gender").getValue(String::class.java)
                val age = snapshot.child("age").getValue(Int::class.java)
                val weight = snapshot.child("weight").getValue(String::class.java)
                val height = snapshot.child("height").getValue(String::class.java)
                val username = snapshot.child("username").getValue(String::class.java)
                view?.findViewById<EditText>(R.id.etUsername)?.setText(username ?: "")

                val ivProfileImage = view?.findViewById<ShapeableImageView>(R.id.ivProfileImage)
                val etWeight = view?.findViewById<EditText>(R.id.etWeight)
                val etHeight = view?.findViewById<EditText>(R.id.etHeight)

                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.pf_img_nobg)
                        .into(ivProfileImage!!)
                }

                etWeight?.setText(weight ?: "")
                etHeight?.setText(height ?: "")

                val genderViews = mapOf(
                    "Male" to R.id.ivMale,
                    "Female" to R.id.ivFemale,
                    "Other" to R.id.ivOther
                )
                val selectedGenderId = genderViews[gender]
                if (selectedGenderId != null) {
                    view?.findViewById<ImageView>(selectedGenderId)?.isSelected = true
                }

                if (age != null && age in 10..100) {
                    val numberPickerContainer =
                        view?.findViewById<LinearLayout>(R.id.numberPickerContainer)
                    val selectedBg = R.drawable.age_circle_selected
                    val unselectedBg = R.drawable.age_circle_unselected

                    for (i in 0 until numberPickerContainer?.childCount!!) {
                        val child = numberPickerContainer.getChildAt(i) as? TextView
                        val number = child?.text?.toString()?.toIntOrNull()
                        if (number == age) {
                            child.background =
                                ContextCompat.getDrawable(requireContext(), selectedBg)
                            selectedView = child
                        } else {
                            child?.background =
                                ContextCompat.getDrawable(requireContext(), unselectedBg)
                        }
                    }
                }
            }
        }.addOnFailureListener {
            (requireActivity() as? BaseActivity)?.errorToast(
                requireContext(),
                "Failed to load profile."
            )
        }
    }


    private fun goToHome() {
        val intent = Intent(requireContext(), HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}