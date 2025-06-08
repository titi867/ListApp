package com.example.listapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.listapp.adapters.GundamAdapter
import com.example.listapp.databinding.FragmentFavBinding
import com.example.listapp.models.Gundam
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/*
* Fragmento que muestra y gestiona una lista de Gundams favoritos, con capacidad
* para actualizar el estado de favoritos y pull-to-refresh.
*
* (Visualiza los Gundams marcados como favoritos desde Firestore,
* permitiendo actualizaciones en tiempo real y sincronización manual).
* */
class FavFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentFavBinding
    private var gundamList = mutableListOf<Gundam>()
    private lateinit var adapter: GundamAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFavBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
        Obtiene el usuario actualmente autenticado y extrae su UID de Firebase
        (puede ser null si no hay sesión).
         */
        val user = auth.currentUser
        val userId = user?.uid

        // Llama a la función fetchGundams pasando el ID del usuario convertido a String
        fetchGundams(userId.toString())

        /*
        Configura el listener para recargar los Gundams favoritos al activar el gesto
        "pull-to-refresh", usando el ID de usuario actual.
         */
        binding.favSwipeRefreshLayout.setOnRefreshListener {
            fetchGundams(userId.toString())
        }

        /*
        Configura el RecyclerView: establece un LinearLayoutManager, inicializa el adapter
        con la lista de Gundams y el callback para favoritos.
         */
        val recyclerView = binding.rvFavItems
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = GundamAdapter(gundamList) { gundam ->
            toggleFavorite(gundam, userId.toString())
        }
        recyclerView.adapter = adapter
    }

    /*
    Obtiene los Gundams del usuario desde Firestore, filtra los favoritos y actualiza
    el RecyclerView, mostrando errores con Snackbar.

    (Consulta asíncrona que maneja estados de carga y fallos,
    con actualización manual del adaptador).
     */
    private fun fetchGundams(userId: String) {
        // Activa el indicador visual de carga (spinner) en el SwipeRefreshLayout.
        binding.favSwipeRefreshLayout.isRefreshing = true

        /*
        Consulta los Gundams del usuario en Firestore, filtra los favoritos y
        actualiza el RecyclerView, manejando éxito/error.
         */
        firestore.collection("users")
            .document(userId.toString())
            .collection("gundams")
            .get()
            .addOnSuccessListener { result ->
                gundamList.clear()
                /*
                Recorre los documentos de Firestore, convierte cada uno a objeto Gundam
                y lo añade a la lista solo si está marcado como favorito.
                 */
                for(document in result) {
                    val gundam : Gundam = document.toObject(Gundam::class.java)
                    if(gundam.esFavorito)
                        gundamList.add(gundam)
                }

                /*
                Notifica al adaptador que todos los datos cambiaron,
                forzando un redibujado completo del RecyclerView.
                 */
                adapter.notifyDataSetChanged()
                // Desactiva el indicador visual de carga (spinner) en el SwipeRefreshLayout.
                binding.favSwipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener {
                Snackbar.make(binding.root, "Error loading Gundams", Snackbar.LENGTH_SHORT).show()
                binding.favSwipeRefreshLayout.isRefreshing = false
            }
    }

    /*
    Alterna el estado de favorito de un Gundam en Firestore y actualiza la lista local,
    mostrando notificaciones de éxito/error.

    Flujo completo: valida ID -> actualiza Firestore -> sincroniza estado local -> refresca UI.
     */
    private fun toggleFavorite(gundam:Gundam, userId:String){
        val gundamId = gundam.id
        if(gundamId.isNullOrEmpty()){
            Snackbar.make(binding.root, "Error: gundam id is missing", Snackbar.LENGTH_SHORT).show()
            return
        }

        val newStatus = !gundam.esFavorito
        /*
        Actualiza el estado "favorito" de un Gundam en Firestore y sincroniza los
        cambios localmente, mostrando notificaciones de éxito/error.
        */
        firestore.collection("users")
            .document(userId.toString())
            .collection("gundams")
            .document(gundamId)
            .update("esFavorito", newStatus)
            .addOnSuccessListener {
                gundam.esFavorito = newStatus
                if(!gundam.esFavorito){
                    gundamList.remove(gundam)
                }
                adapter.notifyDataSetChanged()
                Snackbar.make(binding.root, "Gundam favorite updated", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Snackbar.make(binding.root, "Error updating favorite", Snackbar.LENGTH_SHORT).show()
            }
    }

}
