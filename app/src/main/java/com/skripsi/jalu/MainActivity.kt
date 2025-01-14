package com.skripsi.jalu

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.skripsi.jalu.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val customToolbar: Toolbar = findViewById(R.id.customToolbar)
        setSupportActionBar(customToolbar)
        val toolbarTitleTextView: TextView = findViewById(R.id.toolbar_title)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_dashboard,
                R.id.navigation_monitoring,
                R.id.navigation_laporan,
                R.id.navigation_profile
            )
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val destinationTitle = when (destination.id) {
                R.id.navigation_dashboard -> getString(R.string.title_dashboard)
                R.id.navigation_monitoring -> getString(R.string.title_monitoring)
                R.id.navigation_laporan -> getString(R.string.title_laporan)
                R.id.navigation_profile -> getString(R.string.title_profile)
                else -> getString(R.string.app_name)
            }
            toolbarTitleTextView.text = destinationTitle
        }

        navView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_dashboard,
                R.id.navigation_laporan,
                R.id.navigation_monitoring,
                R.id.navigation_profile,
                -> {
                    navController.navigate(menuItem.itemId)
                    true
                }
                else -> false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

}
