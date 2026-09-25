package com.faceattend.app

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.faceattend.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    // Login, and the two post-login landing screens, are all top-level: no up arrow.
    private val appBarConfiguration = AppBarConfiguration(
        setOf(R.id.loginFragment, R.id.staffListFragment, R.id.staffHomeFragment)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        applyWindowInsets()

        navController = (supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment)
            .navController
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, _, _ -> invalidateOptionsMenu() }
    }

    /** Android 15 forces edge-to-edge at targetSdk 35, so bars would overlap content. */
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.appBar) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = bars.top, left = bars.left, right = bars.right)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.navHost) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
            insets
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val signedIn = navController.currentDestination?.id != R.id.loginFragment
        menu.findItem(R.id.action_sign_out)?.isVisible = signedIn
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_sign_out) {
            appContainer.sessionManager.signOut()
            navController.navigate(
                R.id.loginFragment,
                null,
                androidx.navigation.navOptions {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            )
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
}
