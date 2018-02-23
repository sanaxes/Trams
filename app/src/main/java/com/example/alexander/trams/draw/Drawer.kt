package com.example.alexander.trams.draw

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Point
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.startActivity
import android.view.Gravity
import android.widget.Button
import android.widget.TextView
import com.example.alexander.trams.R
import com.example.alexander.trams.activities.TransportList
import com.example.alexander.trams.data.Data
import com.example.alexander.trams.logic.Station
import com.google.android.flexbox.FlexboxLayout
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.util.*
import android.view.WindowManager
import android.widget.CheckBox
import com.google.gson.Gson
import org.jetbrains.anko.*

open class Drawer {
    companion object {
        fun createTypeOfStationTextView(context: Context, type: String): TextView {
            val typeStation = TextView(context)
            typeStation.run {
                text = type
                padding = dip(10)
                textSize = 24f
                textColor = ContextCompat.getColor(context, R.color.colorPrimary)
            }
            return typeStation
        }

        fun createRow(context: Context, station: Station): FlexboxLayout {
            val row = FlexboxLayout(context)
            row.run {
                id = station.id
                flexWrap = FlexboxLayout.FLEX_WRAP_NOWRAP
                flexDirection = FlexboxLayout.FLEX_DIRECTION_ROW
                justifyContent = FlexboxLayout.JUSTIFY_CONTENT_SPACE_AROUND
                backgroundColor = ContextCompat.getColor(context, R.color.buttons_light)
            }
            return row
        }

        fun createButton(context: Context, station: Station, listOfStations: ArrayList<Station>): Button {
            val button = Button(context)
            button.run {
                onClick {
                    Collections.sort(listOfStations) { o1, o2 -> o1.id.compareTo(o2.id) }
                    val index = Collections.binarySearch(listOfStations, Station(station.id, station.name)) { o1, o2 -> o1.id.compareTo(o2.id) }
                    Data.station = listOfStations[index]
                    startActivity(context, Intent(context, TransportList::class.java), null)
                }
                id = station.id
                text = station.name
                backgroundColor = Color.TRANSPARENT
                textSize = 14f
                gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
            }
            val size = Point()
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = wm.defaultDisplay
            display.getRealSize(size)
            button.width = size.x / 10 * 8
            return button
        }

        fun createCheckBox(context: Context, station: Station, sharedPref: SharedPreferences): CheckBox {
            val checkbox = CheckBox(context)
            val json = sharedPref.getString(station.id.toString(), "0")
            var currentStationId = -1
            if (!json.equals("0")) {
                currentStationId = Gson().fromJson(json, Station::class.java).id
            }
            if (currentStationId == station.id) {
                checkbox.isChecked = true
            }
            return checkbox;
        }
    }
}