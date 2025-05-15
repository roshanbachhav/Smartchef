package com.example.urreciptionary

import android.content.Intent
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore
import android.util.Base64;
import android.widget.Button
import android.widget.ImageView;
import android.widget.TextView;

import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.urreciptionary.network.ClarifaiResponse
import com.example.urreciptionary.network.RetrofitClerifaiClient

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

import java.io.ByteArrayOutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


class FoodDetectionActivity : BaseActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 101
    private val REQUEST_IMAGE_CAPTURE = 1
    private var imageView: ImageView? = null
    private var textViewLabel: TextView? = null

    private val captureImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val imageBitmap = data?.extras?.get("data") as Bitmap
                imageView?.setImageBitmap(imageBitmap)
                sendImageToClarifaiAPI(imageBitmap)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food_detection)

        imageView = findViewById(R.id.image_view)
        textViewLabel = findViewById(R.id.text_view_label)

        val retakeButton: Button = findViewById(R.id.button_retake)
        retakeButton.setOnClickListener {
            openCamera()
        }
        val captureButton: Button = findViewById(R.id.button_capture)
        captureButton.setOnClickListener {
            openCamera()
            retakeButton.visibility = View.VISIBLE
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        captureImage.launch(cameraIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView?.setImageBitmap(imageBitmap)
            sendImageToClarifaiAPI(imageBitmap)
        }
    }

    private fun sendImageToClarifaiAPI(bitmap: Bitmap) {
        val encodedImage = encodeImageToBase64(bitmap)

        Log.d("Base64EncodedImage", "YOUR_ENCODED_IMAGE_BASE64_STRING: $encodedImage")

        val jsonObject = """
        {
            "inputs": [
                {
                    "data": {
                        "image": {
                            "base64": "$encodedImage"
                        }
                    }
                }
            ]
        }
        """

        val requestBody = jsonObject.toRequestBody("application/json".toMediaType())

        RetrofitClerifaiClient.getApiService().detectFood(requestBody).enqueue(object : Callback<ClarifaiResponse> {
            override fun onResponse(call: Call<ClarifaiResponse>, response: Response<ClarifaiResponse>) {
                if (response.isSuccessful) {
                    val clarifaiResponse = response.body()
                    clarifaiResponse?.let {
                        val concepts = it.outputs[0].data.concepts
                        val imageUrl = it.outputs[0].imageUrl
                        Log.d("Clarifai", "Image URL: $imageUrl")

                        if (concepts.isNotEmpty()) {
                            val label = concepts[0].name
                            runOnUiThread {
                                textViewLabel?.text = "Detected Food: $label"
                                if (!imageUrl.isNullOrEmpty()) {
                                    Glide.with(this@FoodDetectionActivity)
                                        .load(imageUrl)
                                        .into(imageView!!)
                                } else {
                                    imageView?.setImageBitmap(bitmap)
                                }
                            }
                        } else {
                            runOnUiThread {
                                textViewLabel?.text = "No food detected"
                            }
                        }
                    }
                } else {
                    runOnUiThread {
                        textViewLabel?.text = "Error: ${response.message()}"
                    }
                }
            }

            override fun onFailure(call: Call<ClarifaiResponse>, t: Throwable) {
                runOnUiThread {
                    textViewLabel?.text = "Request failed: ${t.message}"
                }
            }
        })
    }


    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}