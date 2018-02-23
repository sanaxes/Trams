package com.example.alexander.trams.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.widget.*
import com.example.alexander.trams.*
import com.example.alexander.trams.data.Data
import com.example.alexander.trams.draw.Drawer
import com.example.alexander.trams.logic.Station
import com.example.alexander.trams.logic.TramStation
import com.example.alexander.trams.logic.TrolleybusStation
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_select_station.*
import net.hockeyapp.android.CrashManager
import net.hockeyapp.android.UpdateManager
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.support.v4.onRefresh
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.util.*

class SelectStation : NavigationViewListener() {

    private lateinit var linearStation: LinearLayout
    private lateinit var listOfStations: ArrayList<Station>
    private lateinit var elements: Elements
    private var countOfTramStations = 0
    private var countOfTrolleybusStations = 0
    private var hasConnection = false
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCustomToolbar()
    }

    override fun onResume() {
        super.onResume()
        setCustomLayout()
        connect()
        addStationsToList()
        fillLayout()
        checkForCrashes()
    }

    private fun setCustomToolbar() {
        setContentView(R.layout.activity_select_station)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        toggle.isDrawerIndicatorEnabled = false
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toggle.setToolbarNavigationClickListener {
            onBackPressed()
        }
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
    }

    private fun setCustomLayout() {
        scrollViewStation.removeAllViews()
        this@SelectStation.linearStation = LinearLayout(this)
        this@SelectStation.linearStation.run {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
        }
        for (i in 0 until nav_view.menu.size()) {
            nav_view.menu.getItem(i).isChecked = false
        }
        SwipeSelect.run {
            setColorSchemeColors(ContextCompat.getColor(this@SelectStation, R.color.colorPrimary), ContextCompat.getColor(this@SelectStation, R.color.colorPrimaryDark))
            onRefresh {
                isRefreshing = true
                postDelayed({
                    isRefreshing = false
                    onResume()
                }, 1000)
            }
        }
    }

    private fun connect() {
        this@SelectStation.hasConnection = true
        val connect = Thread {
            try {
                this@SelectStation.elements = Jsoup.connect("http://m.ettu.ru/stations/${Data.letter.toUpperCase()}").get().allElements
            } catch (e: Exception) {
                e.printStackTrace()
                this@SelectStation.hasConnection = false
            }
        }
        connect.start()
        try {
            connect.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun fillLayout() {
        if (!this@SelectStation.hasConnection) {
            Toast.makeText(this, "Отсутствует подключение к интернету!", Toast.LENGTH_SHORT).show()
            return
        }
        this@SelectStation.sharedPref = this@SelectStation.getSharedPreferences(
                getString(R.string.com_example_alexander_preference_file_key), Context.MODE_PRIVATE)
        for (station in this@SelectStation.listOfStations) {
            val row = Drawer.createRow(this@SelectStation, station)
            val button = Drawer.createButton(this@SelectStation, station, this@SelectStation.listOfStations)
            val checkBox = Drawer.createCheckBox(this@SelectStation, station, this@SelectStation.sharedPref)
            this@SelectStation.setCheckBoxCheckedChanged(checkBox, station)
            row.addView(button)
            row.addView(checkBox)
            this@SelectStation.linearStation.addView(row)
        }
        if (this@SelectStation.countOfTramStations > 0) {
            val tramsLabel = Drawer.createTypeOfStationTextView(this@SelectStation, "Трамваи:")
            linearStation.addView(tramsLabel, 0)
        }
        if (this@SelectStation.countOfTrolleybusStations > 0) {
            val trolleybusLabel = Drawer.createTypeOfStationTextView(this@SelectStation, "Троллейбусы:")
            this@SelectStation.linearStation.addView(trolleybusLabel, this@SelectStation.countOfTramStations + 1)
        }
        scrollViewStation.addView(this@SelectStation.linearStation)
    }

    private fun setCheckBoxCheckedChanged(checkBox: CheckBox, station: Station) {
        checkBox.run {
            id = station.id
            buttonDrawableResource = R.drawable.checkbox_selector
            onCheckedChange { _, isChecked ->
                if (isChecked) {
                    this@SelectStation.sharedPref.edit().putString(station.id.toString(), Gson().toJson(station)).apply()
                    Toast.makeText(this@SelectStation, "Добавлено в избранное!", Toast.LENGTH_SHORT).show()
                } else {
                    this@SelectStation.sharedPref.edit().remove(station.id.toString()).apply()
                }
            }
        }
    }

    private fun addStationsToList() {
        this@SelectStation.countOfTramStations = 0
        this@SelectStation.countOfTrolleybusStations = 0
        var countOfHeadlines = 0
        this@SelectStation.listOfStations = ArrayList()
        for (element in this@SelectStation.elements) {
            if (element.`is`("h3")) {
                countOfHeadlines++
            }
            if (element.hasAttr("href") && element.attr("href").startsWith("/station")) {
                val idStation = Integer.parseInt(element.attr("href").replace("\\D+".toRegex(), ""))
                val nameStation = element.text()
                if (countOfHeadlines == 2) {
                    this@SelectStation.listOfStations.add(TrolleybusStation(idStation, nameStation))
                    this@SelectStation.countOfTrolleybusStations++
                } else {
                    this@SelectStation.listOfStations.add(TramStation(idStation, nameStation))
                    this@SelectStation.countOfTramStations++
                }
            }
        }
    }

    override fun onBackPressed() {
        when (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            true -> drawer_layout.closeDrawer(GravityCompat.START)
            false -> super.onBackPressed()
        }
    }

    public override fun onPause() {
        super.onPause()
        unregisterManagers()
    }

    public override fun onDestroy() {
        super.onDestroy()
        unregisterManagers()
    }

    private fun checkForCrashes() {
        CrashManager.register(this)
    }

    private fun unregisterManagers() {
        UpdateManager.unregister()
    }
}
