package com.example.urreciptionary

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.urreciptionary.network.Meal
import com.google.android.material.chip.Chip
import com.google.android.material.imageview.ShapeableImageView

class MealPlanAdapter(
    private val mealList: List<Meal>,
    private val actionLayout: LinearLayout
): RecyclerView.Adapter<MealPlanAdapter.MealViewHolder>() {
    private var isMultiSelected = false

    inner class MealViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal_plan,parent,false)

        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = mealList[position]
        holder.title.text = meal.title
        holder.title.text = meal.title
        holder.readyTime.text = "⏱️ ${meal.readyInMinutes} mins"
        holder.healthScore.text = "❤️ ${meal.healthScore}"

        Glide.with(holder.itemView.context)
            .load(meal.image)
            .placeholder(R.drawable.image_shimmer)
            .into(holder.imageMeal)

        holder.ivSelected.visibility = if(meal.isSelected) View.VISIBLE else View.GONE

        holder.itemView.setOnLongClickListener {
            isMultiSelected = true
            meal.isSelected = true
            actionLayout.visibility = View.VISIBLE

            val btnIncomplete = (actionLayout.rootView as View).findViewById<Chip>(R.id.btnIncomplete)
            val complete = (actionLayout.rootView as View).findViewById<Chip>(R.id.btnComplete)
            btnIncomplete.visibility = if (meal.completed) View.VISIBLE else View.GONE
            complete.visibility = if (meal.completed) View.GONE else View.VISIBLE


            notifyItemChanged(position)
            true
        }

        holder.itemView.setOnClickListener {
            if(isMultiSelected){
                meal.isSelected = !meal.isSelected
                notifyItemChanged(position)
                if (mealList.none { it.isSelected }) {
                    clearSelection()
                }
            }
        }

        holder.ivSelected.visibility = if (meal.isSelected) View.VISIBLE else View.GONE

        if (meal.completed) {
            holder.itemView.alpha = 0.6f
            holder.title.paintFlags = holder.title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.ivSelected.setImageResource(R.drawable.ic_completed)
            holder.ivSelected.visibility = View.VISIBLE

            val anim = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.fade_scale)
            holder.itemView.startAnimation(anim)

            val checkIcon = holder.ivSelected
            checkIcon.scaleX = 0f
            checkIcon.scaleY = 0f
            checkIcon.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setInterpolator(OvershootInterpolator())
                .start()

            holder.title.animate()
                .alpha(0.6f)
                .setDuration(250)
                .start()
        } else {
            holder.itemView.alpha = 1f
            holder.title.paintFlags = holder.title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.ivSelected.setImageResource(R.drawable.ic_check_circle)
            holder.ivSelected.visibility = if (meal.isSelected) View.VISIBLE else View.GONE
        }
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

    fun markMealsCompleted(completedIds: List<String>) {
        for (meal in mealList) {
            if (completedIds.contains(meal.id.toString())) {
                meal.completed = true
            }
        }
        notifyDataSetChanged()
    }

    fun markMealsIncomplete(incompleteIds: List<String>) {
        for (meal in mealList) {
            if (incompleteIds.contains(meal.id.toString())) {
                meal.completed = false
            }
        }
        notifyDataSetChanged()
    }

}