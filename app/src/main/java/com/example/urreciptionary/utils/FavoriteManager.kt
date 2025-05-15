package com.example.urreciptionary.utils

import android.app.AlertDialog
import android.content.Context
import com.example.urreciptionary.BaseActivity
import com.example.urreciptionary.network.Meal
import com.example.urreciptionary.network.RecipeWithDetails
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

object FavoriteManager {

    private val db = FirebaseDatabase.getInstance().reference

    fun toggleFavorite(
        context: Context,
        recipe: RecipeWithDetails,
        userId: String,
        onResult: (isFavorite: Boolean) -> Unit
    ){
        val favRef = db.child("users").child(userId).child("favorite").child(recipe.id.toString())

        favRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                favRef.removeValue().addOnSuccessListener {
                    (context as? BaseActivity)?.successToast(context, "Removed from favorites")
                    onResult(false)
                }.addOnFailureListener {
                    (context as? BaseActivity)?.errorToast(context, "Failed to remove: ${it.message}")
                }
            } else {
                val recipeMap = mapOf(
                    "id" to recipe.id,
                    "title" to recipe.title,
                    "image" to recipe.image,
                    "readyInMinutes" to recipe.readyInMinutes,
                    "healthScore" to recipe.healthScore,
                    "timestamp" to System.currentTimeMillis()
                )
                favRef.setValue(recipeMap).addOnSuccessListener {
                    (context as? BaseActivity)?.successToast(context, "Added to favorites")
                    onResult(true)
                }.addOnFailureListener {
                    (context as? BaseActivity)?.errorToast(context, "Failed to add to favorites: ${it.message}")
                }
            }
        }.addOnFailureListener {
            (context as? BaseActivity)?.errorToast(context, "Something went wrong: ${it.message}")
        }
    }

    fun confirmAndDeleteFavorite(
        context: Context,
        selectedMeals: List<Meal>,
        deleteMeal: (mealId: String, onDeleted: () -> Unit) -> Unit,
        onAllDeleted: () -> Unit
    ){
        if(selectedMeals.isEmpty()){
            (context as BaseActivity).warningToast(context,"Opps wishlist is empty")
            return
        }

        AlertDialog.Builder(context)
            .setMessage("This food will be permanently deleted from your wishlist. Are you sure?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                var deletedCount = 0
                val totalToDelete = selectedMeals.size

                selectedMeals.forEach { meal ->
                    val mealId = meal.firebaseKey
                    if (!mealId.isNullOrEmpty()) {
                        deleteMeal(mealId) {
                            deletedCount++
                            if (deletedCount == totalToDelete) {
                                onAllDeleted()
                                (context as BaseActivity).successToast(context,"Deleted Food From Favorite")
                            }
                        }
                    }
                }
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}

