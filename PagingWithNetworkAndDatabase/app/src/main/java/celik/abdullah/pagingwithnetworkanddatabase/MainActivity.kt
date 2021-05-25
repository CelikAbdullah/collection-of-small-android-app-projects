package celik.abdullah.pagingwithnetworkanddatabase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import celik.abdullah.pagingwithnetworkanddatabase.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

/*
* If you annotate an Android class with @AndroidEntryPoint,
* then you also must annotate Android classes that depend on it.
* For example, if you annotate a fragment, then you must also annotate
* any activities where you use that fragment.
*
* @AndroidEntryPoint generates an individual Hilt component for each
* Android class in your project. These components can receive dependencies
* from their respective parent classes as described in Component hierarchy.
* */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return
        navController = host.navController
        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean = navController.navigateUp()
}