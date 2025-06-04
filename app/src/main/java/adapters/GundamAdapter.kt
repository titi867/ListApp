package com.example.listapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import com.example.listapp.models.Gundam
import androidx.recyclerview.widget.RecyclerView
import com.example.listapp.R
import android.widget.*
import com.bumptech.glide.Glide
import java.util.Locale


class GundamAdapter(
    private var gundamList: MutableList<Gundam>,
    private val onFavoriteToggle: (Gundam) -> Unit
) : RecyclerView.Adapter<GundamAdapter.GundamViewHolder>(), Filterable {

    private var fullList: MutableList<Gundam> = gundamList.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GundamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_gundam, parent, false)
        return GundamViewHolder(view)
    }

    override fun onBindViewHolder(holder: GundamViewHolder, position: Int) {
        holder.bind(gundamList[position])
    }

    override fun getItemCount(): Int = gundamList.size

    inner class GundamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gundamImage: ImageView = itemView.findViewById(R.id.ivLogo)
        private val gundamName: TextView = itemView.findViewById(R.id.ivName)
        private val gundamDescription: TextView = itemView.findViewById(R.id.ivDescription)
        private val favoriteIcon: ImageView = itemView.findViewById(R.id.ivIsFavorite)

        fun bind(gundam: Gundam) {
            gundamName.text = gundam.nombre
            gundamDescription.text = gundam.tipo

            val logo = when (gundam.nombre.toLowerCase(Locale.ROOT)) {
                "aerial" -> R.drawable.aerial
                "astray" -> R.drawable.astray
                "duel blitz" -> R.drawable.duel_blitz
                "fenice rinascita" -> R.drawable.fenice_rinascita
                "gunpla" -> R.drawable.gunpla
                "kimaris" -> R.drawable.kimaris
                "shin musha" -> R.drawable.shin_musha
                else -> R.drawable.gundam_logo
            }


            Glide.with(itemView.context)
                .load(logo)
                .into(gundamImage)

            favoriteIcon.setImageResource(
                if(gundam.esFavorito)
                    R.drawable.ic_star_filled
                else
                    R.drawable.ic_star_outline
            )

            favoriteIcon.setOnClickListener {
                onFavoriteToggle(gundam)
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.trim()?.lowercase() ?: ""
                val filtered = if (query.isEmpty()) {
                    fullList
                } else {
                    fullList.filter {
                        it.nombre.lowercase().contains(query) || it.tipo.lowercase().contains(query)
                    }.toMutableList()
                }

                val results = FilterResults()
                results.values = filtered
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                gundamList = results?.values as? MutableList<Gundam> ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }

    fun updateList(newList: List<Gundam>) {
        fullList = newList.toMutableList()
        gundamList = fullList.toMutableList()
        notifyDataSetChanged()
    }
}