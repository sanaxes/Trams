package com.example.alexander.trams

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_trams.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.buttonDrawableResource
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.support.v4.onRefresh
import org.jsoup.Jsoup
import java.io.IOException
import java.lang.System.exit
import java.util.ArrayList
import java.util.Collections

class Trams : NavigationViewListener() {
    private lateinit var listOfTransport: ArrayList<Transport>
    private lateinit var checkbox: CheckBox
    private lateinit var selectedStation: Station

    override fun onBackPressed() {
        when (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            true -> drawer_layout.closeDrawer(GravityCompat.START)
            false -> super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        scrollViewTrams.removeAllViews()
        setTrams()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trams)
        setSupportActionBar(toolbar)
        toolbar.subtitle = SelectedStation.station.name
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
        selectedStation = SelectedStation.station
        checkbox = CheckBox(this)
        val sharedPref = this.getSharedPreferences(
                getString(R.string.com_example_alexander_preference_file_key), Context.MODE_PRIVATE)
        val json = sharedPref.getString(selectedStation.id.toString(), "0")
        var currentStationId = -1
        if (json != "0") {
            val st = Gson().fromJson(json, Station::class.java)
            currentStationId = st.id
        }
        if (currentStationId == selectedStation.id)
            checkbox.isChecked = true
        checkbox.run {
            setPadding(0, 10, 10, 0)
            buttonDrawableResource = R.drawable.checkbox_selector
            onCheckedChange { _, isChecked ->
                if (isChecked) {
                    sharedPref.edit().putString(selectedStation.id.toString(), Gson().toJson(selectedStation)).apply()
                    Toast.makeText(this@Trams, "Добавлено в избранное!", Toast.LENGTH_SHORT).show()
                } else
                    sharedPref.edit().remove(selectedStation.id.toString()).apply()
            }
        }
//        toolbar.addView(checkbox)
//        val flex = FlexboxLayout(this)
//        flex.flexDirection = FlexboxLayout.FLEX_DIRECTION_ROW
//        flex.flexWrap = FlexboxLayout.FLEX_WRAP_NOWRAP
//        flex.justifyContent = FlexboxLayout.JUSTIFY_CONTENT_FLEX_START
//        flex.backgroundColor = Color.RED
//        val size = Point()
//        windowManager.defaultDisplay.getSize(size)
//        val width = size.x
//        flex.layoutParams = FlexboxLayout.LayoutParams(width - toolbar.width, FlexboxLayout.LayoutParams.MATCH_PARENT)
//        Log.e("size", "${toolbar.width}")
//        flex.addView(checkbox)
        toolbar.addView(checkbox)
        setTrams()
    }

    private fun setTrams() {
        val table = TableLayout(this)
        table.run {
            isStretchAllColumns = true
            isShrinkAllColumns = true
        }
        listOfTransport = ArrayList()

        val rowTitle = TableRow(this)
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        val width = size.x
        rowTitle.layoutParams = TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)
        val flexboxLayout = FlexboxLayout(this)
        flexboxLayout.run {
            flexDirection = FlexboxLayout.FLEX_DIRECTION_ROW
            flexWrap = FlexboxLayout.FLEX_WRAP_NOWRAP
            justifyContent = FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND
            setPadding(10, 20, 10, 20)
        }
        val layoutParams = LinearLayout.LayoutParams(width / 8, width / 8)
        val transportIcon = ImageView(this)
        when (selectedStation) {
            is TramStation -> transportIcon.imageResource = R.drawable.ic_tram
            is TrolleybusStation -> transportIcon.imageResource = R.drawable.ic_trolleybus
        }
        transportIcon.layoutParams = layoutParams
        val timeIcon = ImageView(this)
        timeIcon.layoutParams = layoutParams
        timeIcon.imageResource = R.drawable.ic_time
        val distanceIcon = ImageView(this)
        distanceIcon.imageResource = R.drawable.ic_distance
        distanceIcon.layoutParams = layoutParams
        flexboxLayout.run {
            addView(transportIcon)
            addView(timeIcon)
            addView(distanceIcon)
        }
        rowTitle.addView(flexboxLayout)
        table.addView(rowTitle)
        var hasError = false
        val downloadThread = Thread {
            try {
                val doc = Jsoup.connect("http://m.ettu.ru/station/${selectedStation.id}").get()
                val elements = doc.select("div div div")
                for (i in 0 until elements.size step 3) {
                    listOfTransport.add(Tram(
                            Integer.parseInt(elements[i].text()),
                            IntegerParser.getIntFromString(elements[i + 1].text()),
                            IntegerParser.getIntFromString(elements[i + 2].text())
                    ))
                }
            } catch (e: IOException) {
                hasError = true
                e.printStackTrace()
            }
        }
        downloadThread.start()
        try {
            downloadThread.join()
            swipeLayout.run {
                setColorSchemeColors(ContextCompat.getColor(this@Trams, R.color.colorPrimary), ContextCompat.getColor(this@Trams, R.color.colorPrimaryDark))
                onRefresh {
                    isRefreshing = true
                    postDelayed({
                        scrollViewTrams.removeAllViews()
                        isRefreshing = false
                        setTrams()
                    }, 1000)
                }
            }
            if (hasError) {
                Toast.makeText(this, "Отсутствует подключение к интернету!", Toast.LENGTH_SHORT).show()
                return
            }
            Collections.sort(listOfTransport) { o1, o2 -> o1.time.compareTo(o2.time) }
            var even = true
            for (transport in listOfTransport) {
                val tramRow = TableRow(this)
                rowTitle.layoutParams = TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.WRAP_CONTENT)
                val flex = FlexboxLayout(this)
                flex.run {
                    flexDirection = FlexboxLayout.FLEX_DIRECTION_ROW
                    flexWrap = FlexboxLayout.FLEX_WRAP_NOWRAP
                    justifyContent = FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND
                    alignItems = FlexboxLayout.ALIGN_ITEMS_CENTER
                    alignContent = FlexboxLayout.ALIGN_CONTENT_CENTER
                    setPadding(10, 20, 10, 20)
                    if (even)
                        backgroundColor = Color.parseColor("#E1F2F1")
                    even = !even
                }
                val idTram = TextView(this)
                idTram.run {
                    this@run.width = width / 3
                    textSize = 24f
                    gravity = Gravity.CENTER
                    text = "№ ${transport.id}"
                }
                val timeTram = TextView(this)
                timeTram.run {
                    this@run.width = width / 3
                    textSize = 24f
                    gravity = Gravity.CENTER
                    text = "${transport.time} мин"
                }

                val distanceTram = TextView(this)
                distanceTram.run {
                    this@run.width = width / 3
                    textSize = 24f
                    gravity = Gravity.CENTER
                    text = "${transport.distance} м"
                }
                flex.run {
                    addView(idTram)
                    addView(timeTram)
                    addView(distanceTram)
                }
                tramRow.addView(flex)
                table.addView(tramRow)
            }
            scrollViewTrams.addView(table)
        } catch (e: InterruptedException) {
            e.printStackTrace()
            exit(0)
        }

    }
}
