import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.urreciptionary.R
import com.example.urreciptionary.network.RecipeResponse

class HorizontalRecipeAdapter(private val recipes: List<RecipeResponse.Recipe>) :
    RecyclerView.Adapter<HorizontalRecipeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_items, parent, false)
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
    }

    override fun getItemCount() = recipes.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.recipe_image)
        val title: TextView = itemView.findViewById(R.id.recipe_title)
    }
}