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

//El adapter actua como puente entre los datos del gundamList y las vistas, determinando como
//se muestra cada elemento de la lista
class GundamAdapter(
    private var gundamList: MutableList<Gundam>,//Lista de gundams
    private val onFavoriteToggle: (Gundam) -> Unit //calback para activar o desactivar gundam favorito
) : RecyclerView.Adapter<GundamAdapter.GundamViewHolder>(), Filterable {

    //esta lista sirve para mantener una copia de respaldo del listado de gundams
    private var fullList: MutableList<Gundam> = gundamList.toMutableList()

    //Infla (Convierte XML en vistas) el layout layout_gundam para cada item del RecyclerView y
    // devuelve un nuevo ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GundamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_gundam, parent, false)
        return GundamViewHolder(view)
    }

    //oma un modelo Gundam de la lista y lo "pinta" en las vistas del ViewHolder
    override fun onBindViewHolder(holder: GundamViewHolder, position: Int) {
        holder.bind(gundamList[position])
    }

    override fun getItemCount(): Int = gundamList.size

    /*
    * Clase que define cómo mostrar cada Gundam en la lista: asigna nombre, tipo, imagen
    * y maneja clics en favoritos.
    * (ViewHolder que "pinta" los datos del modelo Gundam en las vistas y gestiona interacciones)
    * */
    inner class GundamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val gundamImage: ImageView = itemView.findViewById(R.id.ivLogo)
        private val gundamName: TextView = itemView.findViewById(R.id.ivName)
        private val gundamDescription: TextView = itemView.findViewById(R.id.ivDescription)
        private val favoriteIcon: ImageView = itemView.findViewById(R.id.ivIsFavorite)

        /*
        * Asigna los datos del Gundam (nombre, tipo, imagen y estado de favorito) a las vistas
        * del elemento de la lista y configura el clic del ícono de favorito.
        * */
        fun bind(gundam: Gundam) {
            gundamName.text = gundam.nombre
            gundamDescription.text = gundam.tipo

            val logo = when (gundam.nombre.lowercase()) {
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

    /*
    * Implementa el filtrado de la lista: busca coincidencias en nombres/tipos de Gundams y
    * actualiza la vista con los resultados.
    * */
    override fun getFilter(): Filter {
        return object : Filter() {
            /*
            * Filtra la lista completa de Gundams comparando el texto de búsqueda con nombres y
            * tipos, devolviendo los resultados coincidentes (o la lista completa
            * si no hay búsqueda)
            */
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

            /*
            * Actualiza la lista visible del adapter con los resultados filtrados y notifica
            * al RecyclerView para refrescar la vista.
            * */
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                gundamList = results?.values as? MutableList<Gundam> ?: mutableListOf()
                notifyDataSetChanged()
            }
        }
    }

    /*
    * Reemplaza completamente ambas listas (la original y la visible) con nuevos datos
    * y actualiza toda la vista del RecyclerView.
    * */
    fun updateList(newList: List<Gundam>) {
        fullList = newList.toMutableList()
        gundamList = fullList.toMutableList()
        notifyDataSetChanged()
    }
}