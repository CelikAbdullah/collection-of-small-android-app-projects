package celik.abdullah.navigationwithbottomnavigationmenuandoverflowmenu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import celik.abdullah.navigationwithbottomnavigationmenuandoverflowmenu.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
        * Call the activity's setSupportActionBar() method, and pass the activity's toolbar.
        * This method sets the toolbar as the app bar for the activity
        * */
        setSupportActionBar(binding.toolbar)

        /*
        * NavHostFragment: displays different destinations from your Navigation Graph.
        * */
        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return

        /*
        * NavController (Kotlin/Java object):
        * This is an object that keeps track of the current position
        * within the navigation graph. It orchestrates swapping
        * destination content in the NavHostFragment as you move through
        * a navigation graph.
        *
        * When you navigate, you'll use the NavController object, telling
        * it where you want to go or what path you want to take in your
        * Navigation Graph. The NavController will then show the appropriate
        * destination in the NavHostFragment.
        * */
        navController = host.navController

        /*
        * NavigationUI uses an AppBarConfiguration object to manage the behavior of the Navigation
        * button in the upper-left corner of your app's display area. The Navigation button’s behavior
        * changes depending on whether the user is at a top-level destination.
        *
        * A top-level destination is the root, or highest level destination, in a set of
        * hierarchically-related destinations. Top-level destinations do not display an Up button in
        * the top app bar because there is no higher level destination. By default, the start destination
        * of your app is the only top-level destination.
        * */
        appBarConfiguration = AppBarConfiguration(setOf(R.id.homeFragment, R.id.shopFragment, R.id.notificationsFragment))

        /*
        * Sets up the ActionBar for use with a NavController.
        * By calling this method, the title in the action bar will automatically be updated when the
        * destination changes (assuming there is a valid label).
        *
        * The AppBarConfiguration you provide controls how the Navigation button is displayed.
        * */
        setupActionBarWithNavController(navController, appBarConfiguration)

        /*
        * Sets up a BottomNavigationView for use with a NavController. This will call
        * [android.view.MenuItem.onNavDestinationSelected] when a menu item is selected.
        * The selected item in the NavigationView will automatically be updated when the destination
        * changes.
        * */
        binding.bottomNavView.setupWithNavController(navController)
    }

    /*
    * To specify the options menu for a fragment, override onCreateOptionsMenu()
    * In this method, you can inflate your menu resource (defined in XML) into the Menu
    * provided in the callback
    * */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.overflow_menu, menu)
        return true
    }

    /*
    * NavigationUI also provides helpers for tying destinations to menu-driven UI components.
    * NavigationUI contains a helper method, onNavDestinationSelected(), which takes a MenuItem
    * along with the NavController that hosts the associated destination. If the id of the MenuItem
    * matches the id of the destination, the NavController can then navigate to that destination.
    * */
    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)

    /* Override onSupportNavigateUp() to handle Up navigation */
    override fun onSupportNavigateUp(): Boolean = navController.navigateUp(appBarConfiguration)

}