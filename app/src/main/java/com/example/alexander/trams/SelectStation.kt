package com.example.alexander.trams

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.widget.*
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_select_station.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.onRefresh
import org.jsoup.Jsoup
import java.lang.System.exit
import java.util.*

class SelectStation : NavigationViewListener() {

    private lateinit var listOfStations: ArrayList<Station>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        setStations()

    }

    override fun onResume() {
        super.onResume()
        for (i in 0 until nav_view.menu.size()) {
            nav_view.menu.getItem(i).isChecked = false
        }
        scrollViewStation.removeAllViews()
        setStations()
    }

    private fun setStations() {
        val letter = MainActivity.Companion.letter
        var countOfTrams = 0
        var countOfTrolleybus = 0
        var hasError = false
        val thread = Thread {
            try {
                listOfStations = ArrayList()
                val doc = Jsoup.connect("http://m.ettu.ru/stations/${letter.toUpperCase()}").get()
                val elements = doc.allElements
                var isEnd = 0
                for (e in elements) {
                    if (e.`is`("h3"))
                        isEnd++
                    if (e.hasAttr("href") && e.attr("href").startsWith("/station")) {
                        val idStation = IntegerParser.getIntFromString(e.attr("href"))
                        val nameStation = e.text()
                        if (isEnd == 2) {
                            listOfStations.add(TrolleybusStation(idStation, nameStation))
                            countOfTrolleybus++
                        } else {
                            listOfStations.add(TramStation(idStation, nameStation))
                            countOfTrams++
                        }
                    }
                }
            } catch (e: Exception) {
                hasError = true
            }
        }
        thread.start()
        try {
            thread.join()
            SwipeSelect.run {
                setColorSchemeColors(ContextCompat.getColor(this@SelectStation, R.color.colorPrimary), ContextCompat.getColor(this@SelectStation, R.color.colorPrimaryDark))
                onRefresh {
                    isRefreshing = true
                    postDelayed({
                        scrollViewStation.removeAllViews()
                        isRefreshing = false
                        setStations()
                    }, 1000)
                }
            }
            if (hasError) {
                Toast.makeText(this, "Отсутствует подключение к интернету!", Toast.LENGTH_SHORT).show()
                return
            }
            val linearStation = LinearLayout(this)
            linearStation.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            linearStation.orientation = LinearLayout.VERTICAL
            for (station in listOfStations) {
                val components = FlexboxLayout(this)
                components.run {
                    flexWrap = FlexboxLayout.FLEX_WRAP_NOWRAP
                    flexDirection = FlexboxLayout.FLEX_DIRECTION_ROW
                    justifyContent = FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND
                    backgroundColor = ContextCompat.getColor(this@SelectStation, R.color.buttons_light)
                }
                val btn = Button(this)
                btn.run {
                    onClick {
                        val selectedStationId = station.id
                        val selectedStationName = station.name
                        Collections.sort(listOfStations) { o1, o2 -> o1.id.compareTo(o2.id) }
                        val index = Collections.binarySearch(listOfStations, Station(selectedStationId, selectedStationName)) { o1, o2 -> o1.id.compareTo(o2.id) }
                        SelectedStation.station = listOfStations[index]
                        startActivity<Trams>()
                    }
                    id = station.id
                    text = station.name
                    backgroundColor = Color.TRANSPARENT
                    textSize = 14f
                    gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                }
                val size = Point()
                windowManager.defaultDisplay.getSize(size)
                val width = size.x
                btn.width = width / 10 * 8
                val checkbox = CheckBox(this)
                val sharedPref = this.getSharedPreferences(
                        getString(R.string.com_example_alexander_preference_file_key), Context.MODE_PRIVATE)
                val json = sharedPref.getString(station.id.toString(), "0")
                var currentStationId = -1
                if (json != "0") {
                    val st = Gson().fromJson(json, Station::class.java)
                    currentStationId = st.id
                }
                if (currentStationId == station.id)
                    checkbox.isChecked = true
                checkbox.run {
                    id = station.id
                    buttonDrawableResource = R.drawable.checkbox_selector
                    onCheckedChange { _, isChecked ->
                        if (isChecked) {
                            sharedPref.edit().putString(station.id.toString(), Gson().toJson(station)).apply()
                            Toast.makeText(this@SelectStation, "Добавлено в избранное!", Toast.LENGTH_SHORT).show()
                        } else
                            sharedPref.edit().remove(station.id.toString()).apply()
                    }
                }
                components.addView(btn)
                components.addView(checkbox)
                linearStation.addView(components)
            }
            if (countOfTrams > 0) {
                val tramsLabel = TextView(this)
                tramsLabel.run {
                    text = "Трамваи:"
                    padding = dip(10)
                    textColor = ContextCompat.getColor(this@SelectStation, R.color.colorPrimary)
                    textSize = 24f
                }
                linearStation.addView(tramsLabel, 0)
            }
            if (countOfTrolleybus > 0) {
                val trolleybusLabel = TextView(this)
                trolleybusLabel.run {
                    text = "Троллейбусы:"
                    padding = dip(10)
                    textSize = 24f
                    textColor = ContextCompat.getColor(this@SelectStation, R.color.colorPrimary)
                }
                linearStation.addView(trolleybusLabel, countOfTrams + 1)
            }
            scrollViewStation.addView(linearStation)

        } catch (e: InterruptedException) {
            e.printStackTrace()
            exit(0)
        }
    }

    override fun onBackPressed() {
        when (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            true -> drawer_layout.closeDrawer(GravityCompat.START)
            false -> super.onBackPressed()
        }
    }

}
