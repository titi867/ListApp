package com.example.listapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.listapp.adapters.GundamAdapter
import com.example.listapp.databinding.FragmentListBinding
import com.example.listapp.models.Gundam
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import kotlin.toString

class ListFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentListBinding
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
        binding = FragmentListBinding.inflate(layoutInflater)
        return binding.root
    }

    // TODO ajustar el layout_gundam
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = auth.currentUser
        val userId = user?.uid

        fetchGundams(userId.toString())

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchGundams(userId.toString())
        }

        val recyclerView = binding.rvItems
        //define la estructura de cómo se va a estructurar la lista
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //define la estructura individual de los elementos de la lista
        //adaptando la información de los elementos de la lista a los views

        adapter = GundamAdapter(gundamList) { gundam ->
            toggleFavorite(gundam, userId.toString())
        }
        recyclerView.adapter = adapter

        binding.ivListfragmentLogo.setOnClickListener {

            val newGundam = Gundam(
                Id = UUID.randomUUID().toString(),
                Nombre = "Astray",
                Tipo = "Unit",
                EsFavorito = false
            )

            firestore.collection("users")
                .document(userId.toString())
                .collection("gundams")
                .document(newGundam.Id)
                .set(newGundam)
                .addOnSuccessListener {
                    Snackbar.make(view, "Gundam saved", Snackbar.LENGTH_SHORT).show()
                    fetchGundams(userId.toString())
                }
                .addOnFailureListener{
                    Snackbar.make(view, "Error while saving gundam", Snackbar.LENGTH_SHORT).show()
                }
        }
    }

    private fun fetchGundams(userId: String) {
        binding.swipeRefreshLayout.isRefreshing = true

        firestore.collection("users")
            .document(userId.toString())
            .collection("gundams")
            .get()
            .addOnSuccessListener { result ->
                gundamList.clear()
                for(document in result) {
                    val gundam : Gundam = document.toObject(Gundam::class.java)
                    gundamList.add(gundam)
                }
                adapter.notifyDataSetChanged()
                binding.swipeRefreshLayout.isRefreshing = false

            }
            .addOnFailureListener {
                Snackbar.make(binding.root, "Error loading Gundams", Snackbar.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false
            }
    }

    private fun toggleFavorite(gundam:Gundam, userId:String){
        val gundamId = gundam.Id
        if(gundamId.isNullOrEmpty()){
            Snackbar.make(binding.root, "Error: gundam id is missing", Snackbar.LENGTH_SHORT).show()
            return
        }

        val newStatus = !gundam.EsFavorito
        firestore.collection("users")
            .document(userId.toString())
            .collection("gundams")
            .document(gundamId)
            .update("EsFavorito", newStatus)
            .addOnSuccessListener {
                gundam.EsFavorito = newStatus
                adapter.notifyDataSetChanged()
                Snackbar.make(binding.root, "Gundam favorite updated", Snackbar.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Snackbar.make(binding.root, "Error updating favorite", Snackbar.LENGTH_SHORT).show()
            }

    }


}