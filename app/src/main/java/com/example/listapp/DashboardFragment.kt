package com.example.listapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.listapp.databinding.FragmentDashboardBinding
import com.google.firebase.auth.FirebaseAuth

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var navController: NavController
    private lateinit var onBackPressedCallback : OnBackPressedCallback
    private lateinit var toggle : ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    //    navController = findNavController()

        //obtener el navController desde el navHostFragment (llamada implícita desde el navGraph)
        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.fragment_container_view) as NavHostFragment
        navController = navHostFragment.navController

        //conecta el bottomNavigation a la vista del BottomNavigationView
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)

        binding.navigation.setNavigationItemSelectedListener { menuItem ->
            val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
            if (handled){
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            handled
        }

        binding.floatingActionButton.setOnClickListener {
            navController.navigate(R.id.listFragment)
        }

        onBackPressedCallback = object:OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
                {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    isEnabled = false
                }
            }
        }

    activity?.onBackPressedDispatcher?.addCallback(requireActivity(),
        onBackPressedCallback)

        // (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        toggle = ActionBarDrawerToggle(
            activity, binding.drawerLayout, binding.toolbar, R.string.
            navigation_drawer_open, R.string.navigation_drawer_close)


        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {

                R.id.action_logout -> {
                    FirebaseAuth.getInstance().signOut()

                    val navOptions = NavOptions.Builder().setPopUpTo(R.id.splashScreenFragment, true).build()

                    //Redirigir el loginfragment y limpiar el stacck
                    //Obtenemos el navController definido en el activityMain que es el que usa el login_nav
                    val parentNavController = requireActivity().findNavController(R.id.login_fragment_container_view)
                    parentNavController.navigate(R.id.action_dashboardFragment_to_loginFragment, null, navOptions)
                    true
                }
                else -> false
            }
        }
    }
}