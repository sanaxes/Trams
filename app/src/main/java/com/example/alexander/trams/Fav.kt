package com.example.alexander.trams

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.*
import android.widget.*
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_fav.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.ArrayList
import java.util.Collections

class Fav : NavigationViewListener() {
    private lateinit var listOfStations: ArrayList<Station>
    private lateinit var buttons: ArrayList<FlexboxLayout>
    private lateinit var tramsLabel: TextView
    private lateinit var trolleybusLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fav)
        setSupportActionBar(toolbar)
        toolbar.subtitle = "Избранное"
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        nav_view.menu.getItem(1).isChecked = true
        buttons = ArrayList()
        listOfStations = ArrayList()
        val sharedPref = this.getSharedPreferences(
                getString(R.string.com_example_alexander_preference_file_key), Context.MODE_PRIVATE)
        val mapOfStations = sharedPref.all
        var countOfTrams = 0
        var countOfTrolleybus = 0
        for ((key) in mapOfStations) {
            val json = sharedPref.getString(key, "0")
            if (json != "0") {
                val st = Gson().fromJson(json, Station::class.java)
                if (st.type == "Tram") {
                    listOfStations.add(TramStation(st.id, st.name))
                    countOfTrams++
                } else {
                    listOfStations.add(TrolleybusStation(st.id, st.name))
                    countOfTrolleybus++
                }
            }
        }
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        tramsLabel = TextView(this)
        trolleybusLabel = TextView(this)
        for (station in listOfStations) {
            val components = FlexboxLayout(this)
            components.run {
                flexWrap = FlexboxLayout.FLEX_WRAP_NOWRAP
                flexDirection = FlexboxLayout.FLEX_DIRECTION_ROW
                justifyContent = FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND
                backgroundColor = ContextCompat.getColor(this@Fav, R.color.buttons_light)
                id = station.id
            }
            val btn = Button(this)
            btn.run {
                id = station.id
                text = station.name
                gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                backgroundColor = Color.TRANSPARENT
                textSize = 14f
                this@run.width = width / 10 * 8
                onClick {
                    val selectedStationId = this@run.id
                    val selectedStationName = (it as TextView).text.toString()
                    Collections.sort(listOfStations) { o1, o2 -> o1.id.compareTo(o2.id) }
                    val index = Collections.binarySearch(listOfStations, Station(selectedStationId, selectedStationName)) { o1, o2 -> o1.id.compareTo(o2.id) }
                    SelectedStation.station = listOfStations[index]
                    startActivity<Trams>()
                }
            }
            buttons.add(components)
            val checkbox = CheckBox(this)
            val json = sharedPref.getString(station.id.toString(), "0")
            var currentStation = -1
            if (json != "0") {
                val st = Gson().fromJson(json, Station::class.java)
                currentStation = st.id
            }
            if (currentStation == station.id)
                checkbox.isChecked = true
            checkbox.run {
                gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                id = station.id
                buttonDrawableResource = R.drawable.checkbox_selector
                onCheckedChange { _, isChecked ->
                    if (isChecked) {
                        sharedPref.edit().putString(station.id.toString(), Gson().toJson(station)).apply()
                        Toast.makeText(this@Fav, "Добавлено в избранное!", Toast.LENGTH_SHORT).show()
                    } else {
                        sharedPref.edit().remove(station.id.toString()).apply()
                        linearFav.removeView(linearFav.findViewById(station.id))
                        listOfStations.remove(station)
                        if (countOfTrams == 0 && tramsLabel.parent != null)
                            linearFav.removeView(tramsLabel)
                        if (countOfTrolleybus == 0 && trolleybusLabel.parent != null)
                            linearFav.removeView(trolleybusLabel)
                    }
                }
            }
            components.addView(btn)
            components.addView(checkbox)
            linearFav.addView(components)
        }
        val tramsLabel = TextView(this)
        val trolleybusLabel = TextView(this)
        if (countOfTrams > 0) {
            tramsLabel.run {
                text = "Трамваи:"
                padding = dip(10)
                textColor = ContextCompat.getColor(this@Fav, R.color.colorPrimary)
                textSize = 24f
            }
            linearFav.addView(tramsLabel, 0)
        }
        if (countOfTrolleybus > 0) {
            trolleybusLabel.run {
                text = "Троллейбусы:"
                padding = dip(10)
                textSize = 24f
                textColor = ContextCompat.getColor(this@Fav, R.color.colorPrimary)
            }
            linearFav.addView(trolleybusLabel, countOfTrams + 1)
        }
    }
}