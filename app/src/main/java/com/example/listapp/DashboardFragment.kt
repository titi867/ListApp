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
import androidx.navigation.fragment.findNavController
import com.example.listapp.databinding.FragmentDashboardBinding

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
        navController = findNavController()

        onBackPressedCallback = object:OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
                {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    isEnabled = false
                }
            }
        }

    activity?.onBackPressedDispatcher?.addCallback(requireActivity(), onBackPressedCallback)

        // (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        toggle = ActionBarDrawerToggle(
            activity, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )


        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId) {

                R.id.action_logout -> {
                    navController.navigate(R.id.action_dashboardFragment_to_loginFragment)
                    true
                }
                else -> false
            }
        }
    }
}