package com.example.urreciptionary

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.urreciptionary.network.Meal
import com.example.urreciptionary.utils.FavoriteManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FavoriteFoodActivity : BaseActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var actionLayout: LinearLayout
    private lateinit var searchLayout: LinearLayout
    private lateinit var searchBar: EditText
    private lateinit var emptyLayout: LinearLayout
    private lateinit var favRef: DatabaseReference
    private var mealList: MutableList<Meal> = mutableListOf()
    private lateinit var adapter: FavoriteAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setActivityLayout(R.layout.activity_favorite_food)
        setupBackButton()

        recyclerView = findViewById(R.id.rv_favorite_foods)
        actionLayout = findViewById(R.id.actionLayout)
        searchBar = findViewById(R.id.et_search)
        searchLayout = findViewById(R.id.searchLayout)
        emptyLayout = findViewById(R.id.emptyWishList)


        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = FavoriteAdapter(mealList, actionLayout)
        recyclerView.adapter = adapter


        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        favRef = FirebaseDatabase
            .getInstance()
            .getReference("users")
            .child(currentUserId)
            .child("favorite")

        loadFavoriteMeals()

        searchBar.addTextChangedListener { text ->
            val filteredList = mealList.filter {
                it.title.contains(text.toString(), ignoreCase = true)
            }

            updateUI(filteredList)
        }

        val btnDelete = findViewById<Chip>(R.id.btnDelete)
        btnDelete.setOnClickListener {
            val selectedMeals = adapter.getSelectedMeals()

            FavoriteManager.confirmAndDeleteFavorite(
                context = this,
                selectedMeals = selectedMeals,
                deleteMeal = { mealId, onDeleted ->
                    deleteMeal(mealId)
                    onDeleted()
                },
                onAllDeleted = {
                    adapter.clearSelection()
                    loadFavoriteMeals()
                }
            )
        }

        val goToHomeBtn = findViewById<MaterialButton>(R.id.goToHome)
        goToHomeBtn.setOnClickListener {
            val intent = Intent(this@FavoriteFoodActivity , HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadFavoriteMeals() {
        favRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mealList.clear()

                for (mealSnapshot in snapshot.children) {
                    val meal = mealSnapshot.getValue(Meal::class.java)
                    meal?.setFirebaseKey(mealSnapshot.key)
                    meal?.let { mealList.add(it) }
                }
                if (mealList.isEmpty()) {
                    searchLayout.visibility = View.GONE
                    emptyLayout.visibility = View.VISIBLE
                } else {
                    searchLayout.visibility = View.VISIBLE
                    emptyLayout.visibility = View.GONE
                }

                adapter.notifyDataSetChanged()

                Log.d("FavoriteFoodActivity", "Loaded ${mealList.size} meals")
            }

            override fun onCancelled(error: DatabaseError) {
                errorToast(this@FavoriteFoodActivity, "Failed to load meals")
            }
        })
    }

    private fun updateUI(meals: List<Meal>) {
        adapter = FavoriteAdapter(meals, actionLayout)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun deleteMeal(mealId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val mealRef = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(currentUserId)
            .child("favorite")
            .child(mealId)

        mealRef.removeValue().addOnSuccessListener {
            val indexToRemove = mealList.indexOfFirst { meal -> meal.firebaseKey == mealId }
            if (indexToRemove != -1) {
                mealList.removeAt(indexToRemove)
                adapter.notifyItemRemoved(indexToRemove)
            }
            if (mealList.isEmpty()) {
                searchLayout.visibility = View.GONE
                emptyLayout.visibility = View.VISIBLE
            }
        }.addOnFailureListener {
            errorToast(this, "Failed to delete meal")
        }
    }
}