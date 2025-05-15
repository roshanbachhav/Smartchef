package com.example.urreciptionary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OccasionAdapter(private val occasions: List<String>) :
    RecyclerView.Adapter<OccasionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.occasionIcon)
        val text: TextView = view.findViewById(R.id.occasionText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_occasion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val occasion = occasions[position]
        holder.icon.setImageResource(getIconFromResource(occasion))
        holder.text.text = occasion
    }

    override fun getItemCount() = occasions.size

    private fun getIconFromResource(occasion: String): Int {
        return when (occasion?.lowercase()) {
            "christmas" -> R.drawable.ic_occasion_christmas
            "easter" -> R.drawable.ic_occasion_easter
            "fall" -> R.drawable.ic_occasion_fall
            "thanksgiving" -> R.drawable.ic_occasion_thives
            "halloween" -> R.drawable.ic_occasion_halloween
            "new year’s eve" -> R.drawable.ic_occasion_new_year
            "valentine’s day" -> R.drawable.ic_occasion_valentine
            "hanukkah" -> R.drawable.ic_occasion_hanukkah
            "mother's day" -> R.drawable.ic_occasion_mothers_day
            "father’s day" -> R.drawable.ic_occasion_fathers_day
            "st. patrick’s day" -> R.drawable.ic_occasion_patrics
            "summer" -> R.drawable.ic_occasion_summer
            "winter" -> R.drawable.ic_occasion_winter
            "spring" -> R.drawable.ic_occasion_spring
            else -> R.drawable.ic_occasion_default
        }
    }
}

