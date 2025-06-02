package com.example.listapp.adapters

import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.listapp.models.Gundam
import androidx.recyclerview.widget.RecyclerView
import com.example.listapp.R

class GundamAdapter(
    private val gundamList: List<Gundam>,
    private val onFavoriteClick: (Gundam) -> Unit //callback
)
 : RecyclerView.Adapter<GundamAdapter.ItemViewHolder>() {

        //ViewHolder
        //Recibe una vista y a partir de ahí sacamos las referencias a los views
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivName: TextView = view.findViewById(R.id.ivName)
            val ivDescription: TextView = view.findViewById(R.id.ivDescription)
            val ivItem: ImageView = view.findViewById(R.id.ivLogo)
            val ivEsFavorito : ImageView = view.findViewById(R.id.ivIsFavorite)
        }

    //Reciber la información del paren que utiliza este ItemAdapter,
    //genera o infla la vista y retorna un ItemViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GundamAdapter.ItemViewHolder
    {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_gundam, parent, false)
        return ItemViewHolder(view)
    }

    //Se hace el bind o llenado de información de un item del listado a la vista
    override fun onBindViewHolder(holder: GundamAdapter.ItemViewHolder, position: Int) {
        val gundam = gundamList[position]
        //holder.ivItem.setImageResource(item.ImagenId)
        holder.ivName.text = gundam.Nombre
        holder.ivDescription.text = gundam.Tipo
        holder.ivEsFavorito.setImageResource(
            if(gundam.EsFavorito)
                R.drawable.ic_star_filled
            else
                R.drawable.ic_star_outline
        )
        holder.ivEsFavorito.setOnClickListener {
            onFavoriteClick(gundam)
        }
    }

    //Función que retorna el tamaño del listado
    override fun getItemCount() = gundamList.size
}
