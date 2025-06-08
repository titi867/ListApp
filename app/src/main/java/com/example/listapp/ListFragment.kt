package com.example.listapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.listapp.adapters.GundamAdapter
import com.example.listapp.databinding.FragmentListBinding
import com.example.listapp.models.Gundam
import com.example.listapp.models.SearchViewModel
import com.example.listapp.models.SortOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/*
Fragmento que muestra una lista filtrable/ordenable de Gundams desde Firestore,
con manejo de favoritos y pull-to-refresh.

(Gestiona UI con RecyclerView, conecta con ViewModel para búsquedas/ordenamiento,
y sincroniza datos con Firebase).
 */
class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GundamAdapter
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
    private val gundamList = mutableListOf<Gundam>()
    private val searchViewModel: SearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
        Inicializa el adaptador con la lista de Gundams y el manejador de favoritos,
        y lo asigna al RecyclerView con un LinearLayoutManager.
         */
        adapter = GundamAdapter(gundamList, ::toggleFavorite)
        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItems.adapter = adapter

        //Configura el listener para recargar los datos al activar el gesto de pull-to-refresh.
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchGundams()
        }

        //Ejecuta la carga inicial de los datos de Gundams desde Firestore.
        fetchGundams()

        /*
        Observa cambios en el texto de búsqueda y aplica filtro/ordenamiento
        cada vez que se actualiza.

        (Reacciona dinámicamente a modificaciones en el query del ViewModel).
         */
        searchViewModel.query.observe(viewLifecycleOwner) { query ->
            applyFilterAndSort(query, searchViewModel.sortOption.value)
        }

        searchViewModel.sortOption.observe(viewLifecycleOwner) { sortOption ->
            applyFilterAndSort(searchViewModel.query.value ?: "", sortOption)
        }
    }

    /*
    Recupera los Gundams del usuario desde Firestore, actualizando la UI
    y manejando estados de carga/error.

    (Consulta Firestore -> convierte documentos -> actualiza lista -> aplica filtros -> maneja estados visuales).
     */
    private fun fetchGundams() {
        /*
         Muestra el ProgressBar y oculta otros elementos al iniciar la carga,
         activando también el indicador de refresh.
         */
        binding.progressBar.visibility = View.VISIBLE
        binding.swipeRefreshLayout.visibility = View.GONE
        binding.btnContacto.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = true


        db.collection("users")
            .document(userId)
            .collection("gundams")
            .get()
            .addOnSuccessListener { result ->
                /*
                Convierte cada documento Firestore en un objeto Gundam,
                usando valores por defecto para campos nulos.
                 */
                val items = result.map { doc ->
                    Gundam(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        tipo = doc.getString("tipo") ?: "",
                        esFavorito = doc.getBoolean("esFavorito") ?: false
                    )
                }
                gundamList.clear()
                gundamList.addAll(items)
                /*
                Aplica los filtros y ordenamiento actuales a la lista de Gundams recién cargada.

                (Sincroniza la vista con los parámetros de búsqueda/orden almacenados en el ViewModel).
                 */
                applyFilterAndSort(searchViewModel.query.value ?: "", searchViewModel.sortOption.value)

                // Ocultar ProgressBar, mostrar contenido
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.visibility = View.VISIBLE
                binding.btnContacto.visibility = View.VISIBLE
                binding.swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefreshLayout.visibility = View.VISIBLE
                binding.btnContacto.visibility = View.VISIBLE
                binding.swipeRefreshLayout.isRefreshing = false
            }
    }

    /*
    Alterna el estado de favorito de un Gundam en Firestore y actualiza la lista local al completarse.

    (Cambio remoto → actualización local → refresh completo del RecyclerView).
    */
    private fun toggleFavorite(gundam: Gundam) {
        val newValue = !gundam.esFavorito
        db.collection("users")
            .document(userId)
            .collection("gundams")
            .document(gundam.id)
            .update("esFavorito", newValue)
            .addOnSuccessListener {
                gundam.esFavorito = newValue
                adapter.notifyDataSetChanged()
            }
    }

    /*
    Filtra la lista por nombre (case-insensitive) y aplica el ordenamiento seleccionado (A-Z/Z-A/favoritos).

    (Combina operaciones de filtrado y ordenamiento antes de actualizar el adapter).
     */
    private fun applyFilterAndSort(query: String, sortOption: SortOption?) {
        val filtered = gundamList.filter {
            it.nombre.contains(query, ignoreCase = true)
        }.let {
            when (sortOption) {
                SortOption.NAME_ASC -> it.sortedBy { g -> g.nombre }
                SortOption.NAME_DESC -> it.sortedByDescending { g -> g.nombre }
                SortOption.FAVORITES_FIRST -> it.sortedByDescending { g -> g.esFavorito }
                else -> it
            }
        }

        adapter.updateList(filtered)
    }

    /*
    Limpia la referencia al binding cuando la vista del
    fragmento es destruida para evitar memory leaks.

    (Patrón estándar de View Binding en Fragments).
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
