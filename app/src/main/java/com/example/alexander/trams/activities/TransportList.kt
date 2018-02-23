package com.example.alexander.trams.activities

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.Gravity
import android.widget.*
import com.example.alexander.trams.*
import com.example.alexander.trams.data.Data
import com.example.alexander.trams.logic.*
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.activity_trams.*
import net.hockeyapp.android.CrashManager
import net.hockeyapp.android.UpdateManager
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.support.v4.onRefresh
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class TransportList : NavigationViewListener() {
    private lateinit var listOfTransport: ArrayList<Transport>
    private lateinit var tableLayout: TableLayout
    private lateinit var elements: Elements
    private var hasConnection = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCustomToolbar()
    }

    override fun onResume() {
        super.onResume()
        setCustomLayout()
        connect()
        addTransportToList()
        fillLayout()
        checkForCrashes()
    }

    private fun setCustomToolbar() {
        setContentView(R.layout.activity_trams)
        setSupportActionBar(toolbar)
        toolbar.subtitle = Data.station.name
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
        scrollViewTrams.removeAllViews()
        for (i in 0 until nav_view.menu.size()) {
            nav_view.menu.getItem(i).isChecked = false
        }
        this.tableLayout = TableLayout(this)
        this.tableLayout.run {
            isStretchAllColumns = true
            isShrinkAllColumns = true
        }
        val rowTitle = TableRow(this)
        rowTitle.layoutParams = TableRow.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT)
        val flexboxLayout = FlexboxLayout(this)
        flexboxLayout.run {
            flexDirection = FlexboxLayout.FLEX_DIRECTION_ROW
            flexWrap = FlexboxLayout.FLEX_WRAP_NOWRAP
            justifyContent = FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND
            setPadding(10, 20, 10, 20)
        }
        val transportIcon = createIcon("transport")
        val timeIcon = createIcon("time")
        val distanceIcon = createIcon("distance")
        flexboxLayout.run {
            addView(transportIcon)
            addView(timeIcon)
            addView(distanceIcon)
        }
        rowTitle.addView(flexboxLayout)
        this.tableLayout.addView(rowTitle)
        swipeLayout.run {
            setColorSchemeColors(ContextCompat.getColor(this@TransportList, R.color.colorPrimary), ContextCompat.getColor(this@TransportList, R.color.colorPrimaryDark))
            onRefresh {
                isRefreshing = true
                postDelayed({
                    isRefreshing = false
                    onResume()
                }, 1000)
            }
        }
    }

    private fun createIcon(type: String): ImageView {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        val width = size.x
        val layoutParams = LinearLayout.LayoutParams(width / 8, width / 8)
        val icon = ImageView(this)
        icon.layoutParams = layoutParams
        when (type) {
            "transport" -> {
                when (Data.station) {
                    is TramStation -> icon.imageResource = R.drawable.ic_tram
                    is TrolleybusStation -> icon.imageResource = R.drawable.ic_trolleybus
                }
            }
            "time" -> icon.imageResource = R.drawable.ic_time
            "distance" -> icon.imageResource = R.drawable.ic_distance
        }
        return icon
    }


    private fun connect() {
        this.hasConnection = true;
        val connection = Thread {
            try {
                this.elements = Jsoup.connect("http://m.ettu.ru/station/${Data.station.id}").get().select("div div div")
            } catch (e: IOException) {
                this@TransportList.hasConnection = false
            }
        }
        connection.start()
        try {
            connection.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun addTransportToList() {
        this.listOfTransport = ArrayList()
        for (i in 0 until this.elements.size step 3) {
            this.listOfTransport.add(Tram(
                    Integer.parseInt(this.elements[i].text()),
                    Integer.parseInt(this.elements[i + 1].text().replace("\\D+".toRegex(), "")),
                    Integer.parseInt(this.elements[i + 2].text().replace("\\D+".toRegex(), ""))
            ))
        }
        Collections.sort(this.listOfTransport) { o1, o2 -> o1.time.compareTo(o2.time) }
    }

    private fun fillLayout() {
        if (!this.hasConnection) {
            Toast.makeText(this, "Отсутствует подключение к интернету!", Toast.LENGTH_SHORT).show()
            return
        }

        var even = true
        for (transport in this.listOfTransport) {
            val transportRow = TableRow(this)
            val flex = FlexboxLayout(this)
            flex.run {
                flexDirection = FlexboxLayout.FLEX_DIRECTION_ROW
                flexWrap = FlexboxLayout.FLEX_WRAP_NOWRAP
                justifyContent = FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND
                alignItems = FlexboxLayout.ALIGN_ITEMS_CENTER
                alignContent = FlexboxLayout.ALIGN_CONTENT_CENTER
                setPadding(10, 20, 10, 20)
                if (even) {
                    backgroundColor = Color.parseColor("#E1F2F1")
                }
                even = !even
            }
            val idTransport = createInfoAboutTransport("№ ${transport.id}")
            val timeTransport = createInfoAboutTransport("${transport.time} мин")
            val distanceTransport = createInfoAboutTransport("${transport.distance} м")
            flex.run {
                addView(idTransport)
                addView(timeTransport)
                addView(distanceTransport)
            }
            transportRow.addView(flex)
            this.tableLayout.addView(transportRow)
        }
        scrollViewTrams.addView(this.tableLayout)
    }

    private fun createInfoAboutTransport(infoText: String): TextView {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        val width = size.x
        val info = TextView(this)
        info.run {
            this@run.width = width / 3
            textSize = 24f
            gravity = Gravity.CENTER
            text = infoText
        }
        return info
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
