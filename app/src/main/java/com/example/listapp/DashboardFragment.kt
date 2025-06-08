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
import androidx.fragment.app.viewModels
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
import kotlin.getValue

/*
* Fragmento principal que maneja: navegación con drawer/bottom-nav, perfil de usuario, búsqueda,
* ordenamiento y logout, integrado con Firebase y Navigation Component.
*
* (Controla la UI principal de la app con menús, drawer navigation y
* comunicación con ViewModel para búsquedas/ordenamiento).
* */
class DashboardFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentDashboardBinding
    private lateinit var navController: NavController
    private lateinit var toggle: ActionBarDrawerToggle
    /*
    Inicializa el ViewModel asociado al fragmento usando el delegado by viewModels() de AndroidX.

    (Patrón estándar para obtener una instancia única y compartida del ViewModel que sobrevive a cambios de configuración).
    */
    private val searchViewModel: SearchViewModel by viewModels()

    /*
    * Inicializa Firebase Auth y Firestore,
    * y habilita el menú de opciones en la toolbar del fragmento.
    * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Habilita el menú de opciones en el fragmento para mostrar elementos en la ActionBar/Toolbar.
        setHasOptionsMenu(true)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    /*
    * Infla el layout del fragmento (FragmentDashboardBinding)
    * y devuelve la vista raíz para mostrar la interfaz.
    *
    * (Crea la jerarquía de vistas del dashboard a partir del XML).
    * */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    /*
    * Configura la UI del dashboard: carga datos de usuario,
    * conecta la navegación (drawer/bottom-nav), toolbar y manejo del botón back.
    * */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*
        * Configura el header del NavigationView: carga el nickname del usuario desde Firestore
        * y establece una imagen predeterminada.
        *
        * (Obtiene datos del usuario actual y los muestra en el drawer navigation,
        * con fallback a logo si no hay foto).
        * */
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

        ivProfilePhoto.setImageResource(R.drawable.logo_gundam)

        /*
        * Configura la navegación integrada: vincula el NavController con el BottomNavigation
        * y el NavigationView (drawer).
        * */

        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.fragment_container_view) as NavHostFragment
        navController = navHostFragment.navController

        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)

        NavigationUI.setupWithNavController(binding.navigation, navController)

        /*
        * Maneja clics en el menú del drawer: navega al destino seleccionado
        * y cierra automáticamente el drawer al elegir una opción.
        * */
        binding.navigation.setNavigationItemSelectedListener { menuItem ->
            val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
            if (handled) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            handled
        }

        /*
        * Configura la Toolbar como ActionBar de la actividad y oculta el título predeterminado.
        * */
        (requireActivity() as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        /*
        * Configura el botón hamburguesa que sincroniza el estado del drawer con la Toolbar
        * y añade animación de apertura/cierre.
        * */
        toggle = ActionBarDrawerToggle(
            requireActivity(), binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        /*
        * Maneja el botón back: cierra el drawer si está abierto,
        * o finaliza la actividad si está cerrado.
        * */
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

    /*
    * Configura la barra de búsqueda en la Toolbar: actualiza el ViewModel con cada cambio
    * y navega al listado si es necesario.
    *
    * (Infla el menú de búsqueda y sincroniza el texto ingresado con el ViewModel
    * para filtrado en tiempo real).
    * */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.queryHint = getString(R.string.search)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            /*
            * Procesa la búsqueda confirmada: oculta el teclado, guarda el query
            * en el ViewModel y navega al fragmento de lista si no está visible.
            * */
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                searchViewModel.setQuery(query.orEmpty())

                if (navController.currentDestination?.id != R.id.listFragment) {
                    navController.navigate(R.id.listFragment)
                }
                return true
            }

            /*
            * Actualiza el ViewModel con cada cambio de texto en la búsqueda
            * para filtrado en tiempo real.
            * */
            override fun onQueryTextChange(newText: String?): Boolean {
                searchViewModel.setQuery(newText.orEmpty())
                return true
            }
        })
    }

    /*
    * Selector de acciones para logout y ordenación,
    * con navegación gestionada por Navigation Component.
    * */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                // Cierra la sesión actual del usuario en Firebase Authentication.
                FirebaseAuth.getInstance().signOut()

                /*
                 * Configura y ejecuta la navegación al login, limpiando el backstack
                 * para evitar regresar al dashboard.
                 */
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

    /*
    * Muestra un diálogo de selección para ordenar los resultados (A-Z, Z-A o favoritos)
    * y actualiza el ViewModel con la opción elegida.
    * */
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

                /*
                 * Actualiza el criterio de ordenamiento seleccionado en el ViewModel
                 * para que los datos se reorganicen automáticamente.
                 */
                searchViewModel.setSortOption(selectedOption)

                /*
                * Navega al fragmento de lista solo si no está actualmente visible.
                * */
                if (navController.currentDestination?.id != R.id.listFragment) {
                    navController.navigate(R.id.listFragment)
                }
            }
            .show()
    }
}
