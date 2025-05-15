package com.example.urreciptionary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.urreciptionary.network.Meal
import com.example.urreciptionary.utils.ShowFoodInMealPlanUtils
import com.google.android.material.chip.Chip
import com.google.android.material.imageview.ShapeableImageView


class FavoriteAdapter(
    private var mealList: List<Meal>,
    private val actionLayout: LinearLayout
) : RecyclerView.Adapter<FavoriteAdapter.MealViewHolder>() {
    private var isMultiSelected = false

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageMeal = itemView.findViewById<ShapeableImageView>(R.id.recipe_image)
        val title = itemView.findViewById<TextView>(R.id.tvTitle)
        val readyTime = itemView.findViewById<TextView>(R.id.tvReadyInMinutes)
        val healthScore = itemView.findViewById<TextView>(R.id.tvHealthScore)
        val ivSelected = itemView.findViewById<ImageView>(R.id.ivSelected)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MealViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_meal_plan, parent, false)

        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = mealList[position]
        holder.title.text = meal.title
        holder.readyTime.text = "⏱️ ${meal.readyInMinutes} mins"
        holder.healthScore.text = "❤️ ${meal.healthScore}"

        Glide.with(holder.itemView.context)
            .load(meal.image)
            .placeholder(R.drawable.image_shimmer)
            .into(holder.imageMeal)

        holder.ivSelected.visibility = if (meal.isSelected) View.VISIBLE else View.GONE

        holder.itemView.setOnLongClickListener {
            isMultiSelected = true
            meal.isSelected = true
            actionLayout.visibility = View.VISIBLE

            val btnSavedInMealPlan = actionLayout.findViewById<Chip>(R.id.btnSaveToMealPlan)

            btnSavedInMealPlan.setOnClickListener {
                ShowFoodInMealPlanUtils.saveRecipeId(holder.itemView.context , meal.id)
            }

            notifyItemChanged(position)
            true
        }

        holder.itemView.setOnClickListener {
            if (isMultiSelected) {
                meal.isSelected = !meal.isSelected
                notifyItemChanged(position)

                if (mealList.none { it.isSelected }) {
                    clearSelection()
                }
            }
        }

        holder.ivSelected.visibility = if (meal.isSelected) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = mealList.size

    fun clearSelection() {
        isMultiSelected = false
        mealList.forEach { it.isSelected = false }
        actionLayout.visibility = View.GONE
        notifyDataSetChanged()
    }

    fun getSelectedMeals(): List<Meal> {
        return mealList.filter { it.isSelected }
    }


}