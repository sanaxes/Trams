package com.example.alexander.trams.activities

import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.example.alexander.trams.R
import org.jetbrains.anko.startActivity

open class NavigationViewListener : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.info -> {
                startActivity<Information>()
            }
            R.id.fav -> {
                startActivity<Favorite>()
            }
            R.id.main -> {
                startActivity<SelectLetter>()
            }
            else -> return false
        }
        (findViewById(R.id.drawer_layout) as DrawerLayout).closeDrawer(GravityCompat.START)
        return true
    }
}