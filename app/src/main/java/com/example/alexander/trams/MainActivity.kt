package com.example.alexander.trams

import android.graphics.Point
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.widget.Button
import java.util.ArrayList
import android.widget.Toast
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

class MainActivity : NavigationViewListener() {
    private lateinit var buttons: ArrayList<Button>
    companion object {
        var letter: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        setWords()
        checkForCrashes();
    }

    override fun onResume() {
        super.onResume()
        scrollViewMain.removeAllViews()
        setWords()
        checkForCrashes();
    }

    private fun setWords() {
        nav_view.menu.getItem(0).isChecked = true;
        val flex = FlexboxLayout(this)
        flex.run({
            flexDirection = FlexboxLayout.FLEX_DIRECTION_ROW
            flexWrap = FlexboxLayout.FLEX_WRAP_WRAP
            alignContent = FlexboxLayout.ALIGN_CONTENT_FLEX_START
            alignItems = FlexboxLayout.ALIGN_CONTENT_FLEX_START
            justifyContent = FlexboxLayout.JUSTIFY_CONTENT_FLEX_START
        })
        buttons = ArrayList()
        var hasError = false
        val downloadThread = Thread {
            try {
                val doc = Jsoup.connect("https://m.ettu.ru").get()
                val elems = doc.getElementsByClass("letter-link")
                for (elem in elems) {
                    val size = Point()
                    windowManager.defaultDisplay.getSize(size)
                    val width = size.x
                    val button = Button(this)
                    button.run {
                        onClick {
                            MainActivity.Companion.letter = elem.text()
                            startActivity<SelectStation>()
                        }
                        text = elem.text()
                        layoutParams = FlexboxLayout.LayoutParams(width / 6, width / 6)
                        textSize = 24f
                        backgroundColor = ContextCompat.getColor(this@MainActivity, R.color.buttons_light)
                    }
                    buttons.add(button)
                }
            } catch (e: IOException) {
                hasError = true
                e.printStackTrace()
            }
        }
        downloadThread.start()
        try {
            downloadThread.join()
            SwipeMain.run {
                setColorSchemeColors(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary), ContextCompat.getColor(this@MainActivity, R.color.colorPrimaryDark))
                onRefresh {
                    isRefreshing = true
                    SwipeMain.postDelayed({
                        scrollViewMain.removeAllViews()
                        isRefreshing = false
                        setWords()
                    }, 1000)
                }
            }
            if (hasError) {
                Toast.makeText(this, "Отсутствует подключение к интернету!", Toast.LENGTH_SHORT).show()
                return
            }
            for (btn in buttons)
                flex.addView(btn)
            scrollViewMain.addView(flex)
        } catch (e: InterruptedException) {
            e.printStackTrace()
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
