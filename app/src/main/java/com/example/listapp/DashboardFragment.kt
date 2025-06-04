package com.example.listapp

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.listapp.databinding.FragmentDashboardBinding
import com.example.listapp.models.SearchViewModel
import com.example.listapp.models.SortOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentDashboardBinding
    private lateinit var navController: NavController
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var searchViewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchViewModel = ViewModelProvider(requireActivity())[SearchViewModel::class.java]
        setHasOptionsMenu(true)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navigationView = binding.navigation
        val headerView = navigationView.getHeaderView(0)

        val tvProfileName = headerView.findViewById<TextView>(R.id.tvProfileName)
        val ivProfilePhoto = headerView.findViewById<ImageView>(R.id.ivProfilePhoto)

        firestore.collection("users").document(auth.currentUser?.uid.orEmpty()).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nickname = document.getString("nickname")
                    tvProfileName.text = nickname
                }
            }

        ivProfilePhoto.setImageResource(R.drawable.logo_gundam2)

        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.fragment_container_view) as NavHostFragment
        navController = navHostFragment.navController

        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)

        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        toggle = ActionBarDrawerToggle(
            requireActivity(), binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        binding.drawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        requireActivity().finish()
                    }
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.queryHint = getString(R.string.search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                searchViewModel.setQuery(query.orEmpty())

                if (navController.currentDestination?.id != R.id.listFragment) {
                    navController.navigate(R.id.listFragment)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchViewModel.setQuery(newText.orEmpty())
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()

                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.loginFragment, true)
                    .build()

                findNavController().navigate(
                    R.id.action_dashboardFragment_to_loginFragment,
                    null,
                    navOptions
                )

                true
            }

            R.id.action_sort -> {
                showSortDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSortDialog() {
        val options = arrayOf("Nombre (A-Z)", "Nombre (Z-A)", "Favoritos primero")
        AlertDialog.Builder(requireContext())
            .setTitle("Ordenar por")
            .setItems(options) { _, which ->
                val selectedOption = when (which) {
                    0 -> SortOption.NAME_ASC
                    1 -> SortOption.NAME_DESC
                    2 -> SortOption.FAVORITES_FIRST
                    else -> SortOption.NAME_ASC
                }
                searchViewModel.setSortOption(selectedOption)

                if (navController.currentDestination?.id != R.id.listFragment) {
                    navController.navigate(R.id.listFragment)
                }
            }
            .show()
    }
}
