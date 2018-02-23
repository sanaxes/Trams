package com.example.alexander.trams.activities

import android.graphics.Point
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.widget.Button
import java.util.ArrayList
import android.widget.Toast
import com.example.alexander.trams.data.Data
import com.example.alexander.trams.R
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.activity_main.*
import net.hockeyapp.android.CrashManager
import net.hockeyapp.android.UpdateManager
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.onRefresh
import java.io.IOException
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class SelectLetter : NavigationViewListener() {
    private lateinit var buttons: ArrayList<Button>
    private lateinit var flexBoxLayout: FlexboxLayout
    private var hasConnection = false
    private lateinit var elements: Elements

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCustomToolbar()
    }

    override fun onResume() {
        super.onResume()
        setCustomLayout()
        connect()
        fillLayout()
        checkForCrashes();
    }

    private fun setCustomToolbar() {
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        toolbar.subtitle = "Первая буква остановки"
    }

    private fun setCustomLayout() {
        scrollViewMain.removeAllViews()
        this.flexBoxLayout = FlexboxLayout(this)
        this.flexBoxLayout.run({
            flexDirection = FlexboxLayout.FLEX_DIRECTION_ROW
            flexWrap = FlexboxLayout.FLEX_WRAP_WRAP
            alignContent = FlexboxLayout.ALIGN_CONTENT_FLEX_START
            alignItems = FlexboxLayout.ALIGN_CONTENT_FLEX_START
            justifyContent = FlexboxLayout.JUSTIFY_CONTENT_FLEX_START
        })
        nav_view.menu.getItem(0).isChecked = true
        SwipeMain.run {
            setColorSchemeColors(ContextCompat.getColor(this@SelectLetter, R.color.colorPrimary), ContextCompat.getColor(this@SelectLetter, R.color.colorPrimaryDark))
            onRefresh {
                isRefreshing = true
                SwipeMain.postDelayed({
                    isRefreshing = false
                    onResume()
                }, 1000)
            }
        }
    }

    private fun connect() {
        this.hasConnection = true;
        val connection = Thread {
            try {
                this@SelectLetter.elements = Jsoup.connect("https://m.ettu.ru").get().getElementsByClass("letter-link")
            } catch (e: IOException) {
                this@SelectLetter.hasConnection = false
            }
        }
        connection.start()
        try {
            connection.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun fillLayout() {
        if (!hasConnection) {
            Toast.makeText(this, "Отсутствует подключение к интернету!", Toast.LENGTH_SHORT).show()
            return
        }
        this.buttons = ArrayList()
        for (elem in this.elements) {
            val size = Point()
            windowManager.defaultDisplay.getSize(size)
            val width = size.x
            val button = Button(this)
            button.run {
                onClick {
                    Data.letter = elem.text()
                    startActivity<SelectStation>()
                }
                text = elem.text()
                layoutParams = FlexboxLayout.LayoutParams(width / 6, width / 6)
                textSize = 24f
                backgroundColor = ContextCompat.getColor(this@SelectLetter, R.color.buttons_light)
            }
            this.buttons.add(button)
        }
        for (btn in this.buttons) {
            flexBoxLayout.addView(btn)
        }
        scrollViewMain.addView(flexBoxLayout)
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
