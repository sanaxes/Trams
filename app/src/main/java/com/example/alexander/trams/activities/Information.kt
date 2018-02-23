package com.example.alexander.trams.activities

import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.widget.TextView
import com.example.alexander.trams.R
import kotlinx.android.synthetic.main.activity_info.*

class Information : NavigationViewListener() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        setSupportActionBar(toolbar)
        toolbar.subtitle = "Информация"
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        nav_view.menu.getItem(2).isChecked = true
        for (i in 0 until linearInfo.childCount) {
            val txt = linearInfo.getChildAt(i) as TextView
            txt.setPadding(10, 0, 10, 0)
        }
    }

}
