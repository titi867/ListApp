package com.example.listapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.listapp.adapters.GundamAdapter
import com.example.listapp.databinding.FragmentListBinding
import com.example.listapp.models.Gundam
import com.example.listapp.models.SearchViewModel
import com.example.listapp.models.SortOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

        adapter = GundamAdapter(gundamList) { gundam ->
            toggleFavorite(gundam)
        }

        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItems.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchGundams()
        }

        fetchGundams()

        searchViewModel.query.observe(viewLifecycleOwner) { query ->
            applyFilterAndSort(query, searchViewModel.sortOption.value)
        }

        searchViewModel.sortOption.observe(viewLifecycleOwner) { sortOption ->
            applyFilterAndSort(searchViewModel.query.value ?: "", sortOption)
        }
    }

    private fun fetchGundams() {
        // Mostrar ProgressBar, ocultar contenido
        binding.progressBar.visibility = View.VISIBLE
        binding.swipeRefreshLayout.visibility = View.GONE
        binding.btnContacto.visibility = View.GONE

        getString(R.string.email)
        db.collection("users")
            .document(userId)
            .collection("gundams")
            .get()
            .addOnSuccessListener { result ->
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

    // Aplica búsqueda y ordenamiento
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
