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

        val user = auth.currentUser
        val userId = user?.uid

        fetchGundams(userId.toString())
        binding.favSwipeRefreshLayout.setOnRefreshListener {
            fetchGundams(userId.toString())
        }

        val recyclerView = binding.rvFavItems
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = GundamAdapter(gundamList) { gundam ->
            toggleFavorite(gundam, userId.toString())
        }
        recyclerView.adapter = adapter
    }

    private fun fetchGundams(userId: String) {
        binding.favSwipeRefreshLayout.isRefreshing = true

        firestore.collection("users")
            .document(userId.toString())
            .collection("gundams")
            .get()
            .addOnSuccessListener { result ->
                gundamList.clear()
                for(document in result) {
                    val gundam : Gundam = document.toObject(Gundam::class.java)
                    if(gundam.esFavorito)
                        gundamList.add(gundam)
                }
                adapter.notifyDataSetChanged()
                binding.favSwipeRefreshLayout.isRefreshing = false

            }
            .addOnFailureListener {
                Snackbar.make(binding.root, "Error loading Gundams", Snackbar.LENGTH_SHORT).show()
                binding.favSwipeRefreshLayout.isRefreshing = false
            }
    }

    private fun toggleFavorite(gundam:Gundam, userId:String){
        val gundamId = gundam.id
        if(gundamId.isNullOrEmpty()){
            Snackbar.make(binding.root, "Error: gundam id is missing", Snackbar.LENGTH_SHORT).show()
            return
        }

        val newStatus = !gundam.esFavorito
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
