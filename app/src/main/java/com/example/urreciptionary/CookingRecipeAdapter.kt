package com.example.urreciptionary

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.urreciptionary.network.RecipeResponse

class CookingRecipeAdapter(private val recipes: List<RecipeResponse.Recipe>) :
    RecyclerView.Adapter<CookingRecipeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.horizontal_item_recipe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = recipes[position]

        Log.d("RecipeDetails", "CookingRecipeHealth: ${recipe.healthScore}, ready: ${recipe.readyInMinutes}")

        holder.title.text = recipe.title
        Glide.with(holder.itemView.context)
            .load(recipe.image)
            .placeholder(R.drawable.img_shimmer_background)
            .centerCrop()
            .transform(RoundedCorners(20))
            .override(200, 200)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.image)

        recipe.readyInMinutes.let {
            holder.timeText.text = " $it mins"
        }

        recipe.healthScore.let {
            holder.healthScoreText.text = "Health Score : ${it.toInt()}"
        }

        holder.itemView.setOnClickListener {
            val context: Context = holder.itemView.context
            val intent = Intent(context, FoodDetailsActivity::class.java)
            intent.putExtra("ID", recipe.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = recipes.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.recipe_image)
        val title: TextView = itemView.findViewById(R.id.recipe_title)
        val timeText: TextView = itemView.findViewById(R.id.recipe_time)
        val healthScoreText: TextView = itemView.findViewById(R.id.recipe_health)
    }
}
