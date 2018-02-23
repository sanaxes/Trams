package com.example.alexander.trams.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.ActionBarDrawerToggle
import android.view.*
import android.widget.*
import com.example.alexander.trams.*
import com.example.alexander.trams.draw.Drawer
import com.example.alexander.trams.logic.Station
import com.example.alexander.trams.logic.TramStation
import com.example.alexander.trams.logic.TrolleybusStation
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_fav.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import java.util.ArrayList

class Favorite : NavigationViewListener() {
    private lateinit var tramsLabel: TextView
    private lateinit var trolleybusLabel: TextView
    private lateinit var linearLayoutFavorite: LinearLayout
    private lateinit var listOfStations: ArrayList<Station>
    private var countOfTramStations = 0
    private var countOfTrolleybusStations = 0
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCustomToolbar()
        setCustomLayout()
        fillLayout()
    }

    private fun setCustomLayout() {
        scrollViewFav.removeAllViews()
        this.linearLayoutFavorite = LinearLayout(this)
        this.linearLayoutFavorite.run {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
        }
    }

    private fun setCustomToolbar() {
        setContentView(R.layout.activity_fav)
        setSupportActionBar(toolbar)
        toolbar.subtitle = "Избранное"
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        nav_view.menu.getItem(1).isChecked = true
    }

    private fun fillLayout() {
        this@Favorite.sharedPref = this.getSharedPreferences(
                getString(R.string.com_example_alexander_preference_file_key), Context.MODE_PRIVATE)
        val mapOfStations = this@Favorite.sharedPref.all
        listOfStations = ArrayList()
        for ((key) in mapOfStations) {
            val json = this@Favorite.sharedPref.getString(key, "0")
            if (json != "0") {
                val st = Gson().fromJson(json, Station::class.java)
                if (st.type == "Tram") {
                    this@Favorite.listOfStations.add(0, TramStation(st.id, st.name))
                    this@Favorite.countOfTramStations++
                } else if (st.type == "Trolleybus") {
                    this@Favorite.listOfStations.add(TrolleybusStation(st.id, st.name))
                    this@Favorite.countOfTrolleybusStations++
                }
            }
        }
        for (station in this.listOfStations) {
            val row = Drawer.createRow(this, station)
            val button = Drawer.createButton(this@Favorite, station, this.listOfStations)
            val checkBox = Drawer.createCheckBox(this@Favorite, station, this@Favorite.sharedPref)
            setCheckBoxCheckedChanged(checkBox, station)
            row.addView(button)
            row.addView(checkBox)
            this.linearLayoutFavorite.addView(row)
        }
        this.tramsLabel = Drawer.createTypeOfStationTextView(this, "Трамваи:")
        if (countOfTramStations > 0) {
            linearLayoutFavorite.addView(tramsLabel, 0)
        }
        this.trolleybusLabel = Drawer.createTypeOfStationTextView(this, "Троллейбусы:")
        if (countOfTrolleybusStations > 0) {
            this.linearLayoutFavorite.addView(trolleybusLabel, countOfTramStations + 1)
        }
        scrollViewFav.addView(this.linearLayoutFavorite)
    }

    private fun setCheckBoxCheckedChanged(checkbox: CheckBox, station: Station) {
        checkbox.run {
            gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
            id = station.id
            buttonDrawableResource = R.drawable.checkbox_selector
            onCheckedChange { _, _ ->
                sharedPref.edit().remove(station.id.toString()).apply()
                linearLayoutFavorite.removeView(linearLayoutFavorite.findViewById(station.id))
                listOfStations.remove(station)
                when (station.type) {
                    "Tram" -> this@Favorite.countOfTramStations--
                    "Trolleybus" -> this@Favorite.countOfTrolleybusStations--
                }
                if (this@Favorite.countOfTramStations == 0) {
                    linearLayoutFavorite.removeView(tramsLabel)
                }
                if (this@Favorite.countOfTrolleybusStations == 0) {
                    linearLayoutFavorite.removeView(trolleybusLabel)
                }
                Toast.makeText(this@Favorite, "Удалено из избранного!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}