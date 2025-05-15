package com.example.urreciptionary

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.urreciptionary.network.RecipeWithDetails
import com.example.urreciptionary.utils.FavoriteManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth

class PopularRecipeAdapter(private val recipes: List<RecipeWithDetails>) :
    RecyclerView.Adapter<PopularRecipeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.popular_item_recipe, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recipe = recipes[position]

        holder.title.text = recipe.title

        Glide.with(holder.itemView.context)
            .load(recipe.image)
            .placeholder(R.drawable.img_shimmer_background)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.image)

        holder.time.text = "${recipe.readyInMinutes} mins"
        holder.rating.text = recipe.healthScore.toString()


        holder.fabFavorite.setOnClickListener {

            val context = holder.itemView.context
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId == null) {
                (context as? BaseActivity)?.successToast(context, "Please Login First")
                return@setOnClickListener
            }

            FavoriteManager.toggleFavorite(
                context = context,
                recipe = recipe,
                userId = userId
            ){
                isFavorite ->
                val icon = if (isFavorite) R.drawable.ic_fav_filled else R.drawable.ic_favourite
                holder.fabFavorite.setImageResource(icon)
            }
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
        val fabFavorite: ImageButton = itemView.findViewById(R.id.fabFavorite)
        val time: TextView = itemView.findViewById(R.id.recipe_time)
        val rating: TextView = itemView.findViewById(R.id.recipe_rating)
    }
}