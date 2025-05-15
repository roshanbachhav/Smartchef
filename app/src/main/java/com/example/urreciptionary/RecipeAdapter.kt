import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.urreciptionary.BuildConfig
import com.example.urreciptionary.FoodDetailsActivity
import com.example.urreciptionary.R
import com.example.urreciptionary.network.ApiService
import com.example.urreciptionary.network.Recipe
import com.example.urreciptionary.network.RecipeDetailsResponse
import com.example.urreciptionary.network.RecipeResponse
import com.example.urreciptionary.network.RetrofitClient

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecipeAdapter(
    private val recipes: MutableList<RecipeResponse.Recipe>,
    private val onItemClick: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.horizontal_item_recipe, parent, false)
        return RecipeViewHolder(view)
    }


    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recipeImage: ImageView = itemView.findViewById(R.id.recipe_image)
        val recipeTitle: TextView = itemView.findViewById(R.id.recipe_title)
        val timeText: TextView = itemView.findViewById(R.id.recipe_time)
        val healthScoreText: TextView = itemView.findViewById(R.id.recipe_health)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.recipeTitle.text = recipe.title

        Glide.with(holder.itemView.context)
            .load(recipe.image)
            .placeholder(R.drawable.img_shimmer_background)
            .centerCrop()
            .transform(RoundedCorners(20))
            .into(holder.recipeImage)

        holder.timeText.text = "Loading..."
        holder.healthScoreText.text = "Loading..."

        val apiService = RetrofitClient.getRetrofitInstance().create(ApiService::class.java)
        val apiKey = BuildConfig.API_KEY

        apiService.getRecipeDetails(recipe.id, apiKey)
            .enqueue(object : Callback<RecipeDetailsResponse> {
                override fun onResponse(
                    call: Call<RecipeDetailsResponse>,
                    response: Response<RecipeDetailsResponse>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        holder.timeText.text = "${data?.readyInMinutes} mins"
                        holder.healthScoreText.text = "${data?.healthScore} mins"
                    }
                }

                override fun onFailure(call: Call<RecipeDetailsResponse>, t: Throwable) {
                    holder.timeText.text = "-"
                    holder.healthScoreText.text = "-"
                }

            })


        holder.itemView.setOnClickListener {
            val context: Context = holder.itemView.context
            val intent = Intent(context, FoodDetailsActivity::class.java)
            intent.putExtra("ID", recipe.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = recipes.size

    fun updateList(newList: List<RecipeResponse.Recipe>) {
        recipes.clear()
        recipes.addAll(newList)
        notifyDataSetChanged()
    }

}