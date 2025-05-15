package com.example.urreciptionary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.urreciptionary.network.IngredientSuggestion

class CookingIngredientAdapter(
    private val ingredientSuggestions: List<IngredientSuggestion>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<CookingIngredientAdapter.IngredientViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CookingIngredientAdapter.IngredientViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient_suggestion, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: CookingIngredientAdapter.IngredientViewHolder,
        position: Int
    ) {
        val suggestion = ingredientSuggestions[position]
        holder.bind(suggestion)
    }

    override fun getItemCount(): Int = ingredientSuggestions.size

    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ingredientImage: ImageView = itemView.findViewById(R.id.ingredientImage)
        private val ingredientName: TextView = itemView.findViewById(R.id.ingredientName)

        fun bind(suggestion: IngredientSuggestion) {
            ingredientName.text = suggestion.name
            Glide.with(itemView.context)
                .load("https://spoonacular.com/cdn/ingredients_100x100/${suggestion.image}")
                .circleCrop()
                .into(ingredientImage)

            itemView.setOnClickListener {
                onItemClick(suggestion.name)
            }

        }

    }

}