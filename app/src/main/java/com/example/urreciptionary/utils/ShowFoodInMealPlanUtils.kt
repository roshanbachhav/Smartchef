package com.example.urreciptionary.utils

import android.content.Context
import com.example.urreciptionary.BaseActivity
import com.google.firebase.auth.FirebaseAuth

object ShowFoodInMealPlanUtils {
    fun saveRecipeId(context: Context, recipeId: Int) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val key = currentUser.uid
            val prefs = context.getSharedPreferences("MealPlans", Context.MODE_PRIVATE)
            val idSet = prefs.getStringSet("recipe_ids", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
            idSet.add(recipeId.toString())
            prefs.edit().putStringSet(key, idSet).apply()
            (context as? BaseActivity)?.successToast(context, "Added to meal plan 👍!")
        } else {
            (context as BaseActivity).errorToast(context, "User not logged in")
        }
    }
}